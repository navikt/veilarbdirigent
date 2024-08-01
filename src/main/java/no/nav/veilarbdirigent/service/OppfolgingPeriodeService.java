package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

import static no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient.ProfileringsResultat.*;
import static no.nav.veilarbdirigent.utils.TaskFactory.lagCvJobbprofilAktivitetTask;
import static no.nav.veilarbdirigent.utils.TaskUtils.createTaskIfNotStoredInDb;
import static no.nav.veilarbdirigent.utils.TaskUtils.getStatusFromTry;

@Slf4j
@Service
public class OppfolgingPeriodeService extends KafkaCommonConsumerService<OppfolgingsperiodeDto> {

    private final AktorOppslagClient aktorOppslagClient;

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final ArbeidssoekerregisterClient arbeidssoekerregisterClient;

    private final TaskProcessorService taskProcessorService;

    private final TaskRepository taskRepository;

    public OppfolgingPeriodeService(AktorOppslagClient aktorOppslagClient,
                                    VeilarboppfolgingClient veilarboppfolgingClient,
                                    VeilarbregistreringClient veilarbregistreringClient, ArbeidssoekerregisterClient arbeidssoekerregisterClient,
                                    TaskProcessorService taskProcessorService,
                                    TaskRepository taskRepository) {
        this.aktorOppslagClient = aktorOppslagClient;
        this.veilarboppfolgingClient = veilarboppfolgingClient;
        this.veilarbregistreringClient = veilarbregistreringClient;
        this.arbeidssoekerregisterClient = arbeidssoekerregisterClient;
        this.taskProcessorService = taskProcessorService;
        this.taskRepository = taskRepository;
    }

    @Override
    protected void behandleKafkaMeldingLogikk(OppfolgingsperiodeDto oppfolgingsperiodeDto) {
        if (oppfolgingsperiodeDto.getAktorId().isEmpty() || oppfolgingsperiodeDto.getStartDato() == null) {
            log.warn("Ugyldig data for siste oppfolging periode på bruker: " + oppfolgingsperiodeDto.getAktorId());
            return;
        }
        if (oppfolgingsperiodeDto.getSluttDato() != null && oppfolgingsperiodeDto.getStartDato().isAfter(oppfolgingsperiodeDto.getSluttDato())) {
            log.error("Ugyldig start/slutt dato for siste oppfolging periode på bruker: " + oppfolgingsperiodeDto.getAktorId());
            return;
        }

        if (oppfolgingsperiodeDto.getSluttDato() == null) {
            log.info("Starter oppfolging for: " + oppfolgingsperiodeDto.getAktorId());
            consumeOppfolgingStartet(oppfolgingsperiodeDto.getAktorId(), oppfolgingsperiodeDto.getStartDato(), oppfolgingsperiodeDto.getUuid(), oppfolgingsperiodeDto.getStartetBegrunnelse());
        } else {
            log.info("Avslutter oppfolging for: " + oppfolgingsperiodeDto.getAktorId());
        }
    }

    private void consumeOppfolgingStartet(String aktorIdStr, ZonedDateTime oppfolgingStartDato, UUID oppfolgingsperiodeId, OppfolgingsperiodeDto.StartetBegrunnelseDTO startetBegrunnelse) throws RuntimeException {
        try {

            // TODO: Fjerne, dette er en quick fix for å unngå race condition.
            //  Når man henter siste registrering fra veilarbregistrering,
            //  så har ikke nødvendigvis veilarbregistrering fått svar fra arena og oppdatert så siste registrering er gjeldende
            var date = ZonedDateTime.now().minusMinutes(1);
            if (oppfolgingStartDato.isAfter(date)) {
                Thread.sleep(60000);
            }

            AktorId aktorId = AktorId.of(aktorIdStr);
            Fnr fnr = aktorOppslagClient.hentFnr(aktorId);

            var skalHaCVKort = false;

            if (startetBegrunnelse == OppfolgingsperiodeDto.StartetBegrunnelseDTO.ARBEIDSSOKER) {
                log.info("Behandler oppfølgingStartet for bruker med arbeidssøkerperiode fra nytt arbeidssøkerregister");
                skalHaCVKort = skalOppretteCvKortForArbeidssøker(fnr);
            } else if (startetBegrunnelse == OppfolgingsperiodeDto.StartetBegrunnelseDTO.SYKEMELDT_MER_OPPFOLGING) {
                log.info("Behandler oppfølgingStarter for bruker uten arbeidssøkerperiode og som kanskje er sykmeldt");
                skalHaCVKort = erSykmeldtOgSkalOppretteCvKort(fnr);
            }

            if (skalHaCVKort) {
                Optional<Task> maybeCvJobbprofilAktivitetTask = createTaskIfNotStoredInDb(
                        () -> lagCvJobbprofilAktivitetTask(oppfolgingsperiodeId, aktorId), taskRepository
                );

                if (maybeCvJobbprofilAktivitetTask.isPresent()) {
                    Task cvJobbprofilAktivitetTask = maybeCvJobbprofilAktivitetTask.get();

                    Try<String> cvJobbprofilAktivitetResult = taskProcessorService.processOpprettAktivitetTask(cvJobbprofilAktivitetTask);
                    cvJobbprofilAktivitetTask.setTaskStatus(getStatusFromTry(cvJobbprofilAktivitetResult));

                    log.info("Inserting task for aktorId={} task={}", aktorId, cvJobbprofilAktivitetTask);
                    taskRepository.insert(cvJobbprofilAktivitetTask);
                } else {
                    log.info("No tasks for aktorId={}", aktorId);
                }
            }

            log.info("Finished consuming kafka record for aktorId={}", aktorId);
        } catch (Exception e) {
            throw new RuntimeException("Kan ikke behandle oppfølging startet fra Kafka", e);
        }
    }

    private boolean skalOppretteCvKortForArbeidssøker(Fnr fnr) {
        var sisteSamletInformasjon = arbeidssoekerregisterClient.hentSisteSamletInformasjon(fnr);
        if(sisteSamletInformasjon.arbeidssoekerperiode().isEmpty() || sisteSamletInformasjon.profilering().isEmpty()) {
            return false;
        }
        var sisteArbeidssøkerperiode = sisteSamletInformasjon.arbeidssoekerperiode().get();
        var sisteProfilering = sisteSamletInformasjon.profilering().get();

        var arbeidssøkerperiodeErAvsluttet = sisteArbeidssøkerperiode.avsluttet != null;
        if(arbeidssøkerperiodeErAvsluttet) return false;

        List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);
        var registreringsdato = sisteArbeidssøkerperiode.startet.tidspunkt;
        var erNyligRegistrert = RegistreringUtils.erNyligRegistrert(registreringsdato.toLocalDateTime(), oppfolgingsperioder);

        var profileringerSomTilsierAtCvKortSkalOpprettes = List.of(ANTATT_GODE_MULIGHETER, ANTATT_BEHOV_FOR_VEILEDNING, OPPGITT_HINDRINGER);

        var harRiktigProfilering = profileringerSomTilsierAtCvKortSkalOpprettes.contains(sisteProfilering);
        log.info("Avgjør om CV-kort skal opprettes for arbeidssøker, erNyligRegistrert={}, harRiktigProfilering={}", erNyligRegistrert, harRiktigProfilering);

        return erNyligRegistrert && harRiktigProfilering;
    }


    private boolean erSykmeldtOgSkalOppretteCvKort(Fnr fnr) {
        List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);
        var maybeBrukerRegistrering = veilarbregistreringClient.hentRegistrering(fnr)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
        if (maybeBrukerRegistrering.isEmpty()) {
            return false;
        }

        BrukerRegistreringWrapper brukerRegistrering = maybeBrukerRegistrering.get();
        LocalDateTime registreringsdato = RegistreringUtils.hentRegistreringDato(brukerRegistrering);

        boolean skalIkkeTilbakeTilArbeidsgiver = RegistreringUtils.erSykmeldtOgSkalIkkeTilbakeTilArbeidsgiver(brukerRegistrering);
        boolean erNyligRegistrert = RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder);

        return skalIkkeTilbakeTilArbeidsgiver && erNyligRegistrert;
    }

     private Optional<ArbeidssoekerregisterClient.ArbeidssoekerPeriode> finnGjeldendeArbeidssøkerperiode(ArbeidssoekerregisterClient.SamletInformasjon samletInformasjon) {
        var arbeidssøkerperioder = samletInformasjon.arbeidssoekerperioder;
        if (arbeidssøkerperioder.isEmpty()) return Optional.empty();

        var gjeldendePerioder = arbeidssøkerperioder
                .stream()
                .filter((arbeidssoekerPeriode ->
                        arbeidssoekerPeriode.avsluttet == null && arbeidssoekerPeriode.startet.tidspunkt.isBefore(ZonedDateTime.now())
                ))
                .toList();

        if (gjeldendePerioder.isEmpty()) {
            log.info("Fant ingen gjeldende arbeidssøkerperiode");
            return Optional.empty();
        } else if (gjeldendePerioder.size() > 1) {
            log.info("Fant mer enn en åpne arbeidssøkerperioder. Returnerer den siste");
            return Optional.ofNullable(gjeldendePerioder
                    .stream()
                    .sorted(Comparator.comparing(arbeidssoekerPeriode -> arbeidssoekerPeriode.startet.tidspunkt))
                    .toList()
                    .get(0));
        } else {
            return Optional.ofNullable(gjeldendePerioder.get(0));
        }
    }

    private Optional<ArbeidssoekerregisterClient.ProfileringsResultat> finnSisteProfilering(ArbeidssoekerregisterClient.SamletInformasjon samletInformasjon) {
        var profileringer = samletInformasjon.profileringer;

        if (profileringer.isEmpty()) {
            log.info("Fant ingen profilering");
            return Optional.empty();
        } else if (profileringer.size() == 1) {
            return Optional.of(profileringer.get(0).profilertTil);
        } else {
            var sisteProfilering = profileringer
                    .stream()
                    .filter(profilering -> profilering.profileringSendtInnAv != null)
                    .max(Comparator.comparing(profilering -> profilering.profileringSendtInnAv.tidspunkt));
            return sisteProfilering.map(profilering -> profilering.profilertTil);
        }
    }
}
