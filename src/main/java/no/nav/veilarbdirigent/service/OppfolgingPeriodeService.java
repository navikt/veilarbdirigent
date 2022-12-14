package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static no.nav.veilarbdirigent.utils.TaskFactory.lagCvJobbprofilAktivitetTask;
import static no.nav.veilarbdirigent.utils.TaskFactory.lagKanskjePermittertDialogTask;
import static no.nav.veilarbdirigent.utils.TaskUtils.createTaskIfNotStoredInDb;
import static no.nav.veilarbdirigent.utils.TaskUtils.getStatusFromTry;

@Slf4j
@Service
public class OppfolgingPeriodeService extends KafkaCommonConsumerService<SisteOppfolgingsperiodeV1> {

    private final AktorOppslagClient aktorOppslagClient;

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final TaskProcessorService taskProcessorService;

    private final TaskRepository taskRepository;

    private final JdbcTemplate jdbcTemplate;

    public OppfolgingPeriodeService(AktorOppslagClient aktorOppslagClient,
                                    VeilarboppfolgingClient veilarboppfolgingClient,
                                    VeilarbregistreringClient veilarbregistreringClient,
                                    TaskProcessorService taskProcessorService,
                                    TaskRepository taskRepository,
                                    JdbcTemplate jdbcTemplate){
        this.aktorOppslagClient = aktorOppslagClient;
        this.veilarboppfolgingClient = veilarboppfolgingClient;
        this.veilarbregistreringClient = veilarbregistreringClient;
        this.taskProcessorService = taskProcessorService;
        this.taskRepository = taskRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void mbehandleKafkaMeldingLogikk(SisteOppfolgingsperiodeV1 sisteOppfolgingsperiod) {
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
            if(oppfolgingStartDato.isAfter(date)) {
                Thread.sleep(60000);
            }

            AktorId aktorId = AktorId.of(aktorIdStr);
            Fnr fnr = aktorOppslagClient.hentFnr(aktorId);

            List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);

            Optional<BrukerRegistreringWrapper> maybeBrukerRegistrering = veilarbregistreringClient.hentRegistrering(fnr)
                    .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

            if (maybeBrukerRegistrering.isEmpty()) {
                log.info("Bruker aktorId={} har ikke registrert seg gjennom arbeidssokerregistrering og skal ikke ha aktivitet/dialog", aktorId);
                return;
            }

            BrukerRegistreringWrapper brukerRegistrering = maybeBrukerRegistrering.get();

            LocalDateTime registreringsdato = RegistreringUtils.hentRegistreringDato(brukerRegistrering);

            if (!RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder)) {
                log.info("Bruker {} er ikke nylig registrert og skal ikke ha aktivitet/dialog", aktorId);
                return;
            }

            boolean erNyRegistrert = RegistreringUtils.erNyregistrert(brukerRegistrering);
            boolean erNySykmeldtBrukerRegistrert = RegistreringUtils.erNySykmeldtBrukerRegistrert(brukerRegistrering);

            log.info(
                    "Behandler bruker hvor oppfølging har startet. aktorId={} erNyRegistrert={} erNySykmeldtBrukerRegistrert={}",
                    aktorId, erNyRegistrert, erNySykmeldtBrukerRegistrert
            );

            List<Task> tasksToPerform = new ArrayList<>();

            if (erNyRegistrert) {
                Optional<Task> maybePermittertDialogTask = createTaskIfNotStoredInDb(
                        () -> lagKanskjePermittertDialogTask(oppfolgingsperiodeId.toString(), aktorId), taskRepository
                );

                if (maybePermittertDialogTask.isPresent()) {
                    Task permittertDialogTask = maybePermittertDialogTask.get();

                    Try<String> dialogTaskResult = taskProcessorService.processOpprettDialogTask(permittertDialogTask);
                    permittertDialogTask.setTaskStatus(getStatusFromTry(dialogTaskResult));

                    tasksToPerform.add(permittertDialogTask);
                }
            }

            if (erNySykmeldtBrukerRegistrert || erNyRegistrert) {
                Optional<Task> maybeCvJobbprofilAktivitetTask = createTaskIfNotStoredInDb(
                        () -> lagCvJobbprofilAktivitetTask(oppfolgingsperiodeId.toString(), aktorId), taskRepository
                );

                if (maybeCvJobbprofilAktivitetTask.isPresent()) {
                    Task cvJobbprofilAktivitetTask = maybeCvJobbprofilAktivitetTask.get();

                    Try<String> cvJobbprofilAktivitetResult = taskProcessorService.processOpprettAktivitetTask(cvJobbprofilAktivitetTask);
                    cvJobbprofilAktivitetTask.setTaskStatus(getStatusFromTry(cvJobbprofilAktivitetResult));

                    tasksToPerform.add(cvJobbprofilAktivitetTask);
                }
            }

            if (tasksToPerform.isEmpty()) {
                log.info("No tasks for aktorId={}", aktorId);
            } else {
                log.info("Inserting tasks for aktorId={} tasks={}", aktorId, tasksToPerform);
                taskRepository.insert(tasksToPerform);
            }

            log.info("Finished consuming kafka record for aktorId={}", aktorId);
        }
        catch (Exception e){
            throw new RuntimeException("Kan ikke behandle oppfølging startet fra Kafka");
        }
    }
}
