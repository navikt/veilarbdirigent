package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.utils.DbUtils;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class OppfolgingPeriodeService extends KafkaCommonConsumerService<SisteOppfolgingsperiodeV1> {

    private final AktorOppslagClient aktorOppslagClient;

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final ArbeidssoekerregisterClient arbeidssoekerregisterClient;

    private final TaskProcessorService taskProcessorService;

    private final TaskRepository taskRepository;

    private final JdbcTemplate jdbcTemplate;

    public OppfolgingPeriodeService(AktorOppslagClient aktorOppslagClient,
                                    VeilarboppfolgingClient veilarboppfolgingClient,
                                    VeilarbregistreringClient veilarbregistreringClient, ArbeidssoekerregisterClient arbeidssoekerregisterClient,
                                    TaskProcessorService taskProcessorService,
                                    TaskRepository taskRepository,
                                    JdbcTemplate jdbcTemplate) {
        this.aktorOppslagClient = aktorOppslagClient;
        this.veilarboppfolgingClient = veilarboppfolgingClient;
        this.veilarbregistreringClient = veilarbregistreringClient;
        this.arbeidssoekerregisterClient = arbeidssoekerregisterClient;
        this.taskProcessorService = taskProcessorService;
        this.taskRepository = taskRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void behandleKafkaMeldingLogikk(SisteOppfolgingsperiodeV1 sisteOppfolgingsperiod) {
        if (sisteOppfolgingsperiod.getAktorId().isEmpty() || sisteOppfolgingsperiod.getStartDato() == null) {
            log.warn("Ugyldig data for siste oppfolging periode på bruker: " + sisteOppfolgingsperiod.getAktorId());
            return;
        }
        if (sisteOppfolgingsperiod.getSluttDato() != null && sisteOppfolgingsperiod.getStartDato().isAfter(sisteOppfolgingsperiod.getSluttDato())) {
            log.error("Ugyldig start/slutt dato for siste oppfolging periode på bruker: " + sisteOppfolgingsperiod.getAktorId());
            return;
        }

        if (sisteOppfolgingsperiod.getSluttDato() == null) {
            log.info("Starter oppfolging for: " + sisteOppfolgingsperiod.getAktorId());
            consumeOppfolgingStartet(sisteOppfolgingsperiod.getAktorId(), sisteOppfolgingsperiod.getStartDato(), sisteOppfolgingsperiod.getUuid());
        } else {
            log.info("Avslutter oppfolging for: " + sisteOppfolgingsperiod.getAktorId());
        }
    }

    private void consumeOppfolgingStartet(String aktorIdStr, ZonedDateTime oppfolgingStartDato, UUID oppfolgingsperiodeId) throws RuntimeException {
        try {
            /*
            Siden vi utfører oppgaver som ikke er idempotent før vi lagrer resultatet i databasen, så gjør vi en ekstra sjekk
            på om koblingen til databasen er grei, slik at vi ikke utfører oppgaver og ikke får lagret resultatet.
            */

            if (DbUtils.checkDbHealth(jdbcTemplate).isUnhealthy()) {
                log.error("Health check failed, aborting consumption of kafka record");
                throw new IllegalStateException("Cannot connect to database");
            }
            // TODO: Fjerne, dette er en quick fix for å unngå race condition.
            //  Når man henter siste registrering fra veilarbregistrering,
            //  så har ikke nødvendigvis veilarbregistrering fått svar fra arena og oppdatert så siste registrering er gjeldende
            var date = ZonedDateTime.now().minusMinutes(1);
            if (oppfolgingStartDato.isAfter(date)) {
                Thread.sleep(60000);
            }

            AktorId aktorId = AktorId.of(aktorIdStr);
            Fnr fnr = aktorOppslagClient.hentFnr(aktorId);

            var åpenArbeidssøkerperiode = hentGjeldendeArbeidssøkerperiode(fnr);

            var skalHaCVKort = false;

            if (åpenArbeidssøkerperiode.isPresent()) {
                log.info("Behandler oppfølgingStartet for bruker med arbeidssøkerperiode fra nytt arbeidssøkerregister");
                skalHaCVKort = skalOppretteCvKortForArbeidssøker(fnr, åpenArbeidssøkerperiode.get());
            } else {
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
                }
                else {
                    log.info("No tasks for aktorId={}", aktorId);
                }
            }

            log.info("Finished consuming kafka record for aktorId={}", aktorId);
        } catch (Exception e) {
            throw new RuntimeException("Kan ikke behandle oppfølging startet fra Kafka", e);
        }
    }

    private boolean skalOppretteCvKortForArbeidssøker(Fnr fnr, ArbeidssoekerregisterClient.ArbeidssoekerPeriode arbeidssoekerPeriode) {
        List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);
        var registreringsdato = arbeidssoekerPeriode.startet.tidspunkt;
        var erNyligRegistrert = RegistreringUtils.erNyligRegistrert(registreringsdato.toLocalDateTime(), oppfolgingsperioder);

        var profilering = hentSisteProfilering(fnr, arbeidssoekerPeriode.periodeId);
        var profileringerSomTilsierAtCvKortSkalOpprettes = List.of(ANTATT_GODE_MULIGHETER, ANTATT_BEHOV_FOR_VEILEDNING, OPPGITT_HINDRINGER);

        var harRiktigProfilering = profileringerSomTilsierAtCvKortSkalOpprettes.contains(profilering.get());
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

    private Optional<ArbeidssoekerregisterClient.ArbeidssoekerPeriode> hentGjeldendeArbeidssøkerperiode(Fnr fnr) {
        var arbeidssøkerperioder = arbeidssoekerregisterClient.hentArbeidsoekerPerioder(fnr);
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

    private Optional<ArbeidssoekerregisterClient.ProfileringsResultat> hentSisteProfilering(Fnr fnr, UUID arbeidssøkerperiode) {
        var profileringer = arbeidssoekerregisterClient.hentProfileringer(fnr, arbeidssøkerperiode);

        if (profileringer.isEmpty()) {
            log.info("Fant ingen profilering for arbeidssøkerperiode " + arbeidssøkerperiode);
            return Optional.empty();
        }

        var sisteProfilering = profileringer
                .stream()
                .max(Comparator.comparing(profilering -> profilering.profileringSendtInnAv.tidspunkt))
                .get();

        return Optional.of(sisteProfilering.profilertTil);
    }
}
