package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.utils.DbUtils;
import no.nav.veilarbdirigent.utils.OppfolgingsperiodeUtils;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static no.nav.veilarbdirigent.utils.TaskFactory.*;
import static no.nav.veilarbdirigent.utils.TaskUtils.createTaskIfNotStoredInDb;
import static no.nav.veilarbdirigent.utils.TaskUtils.getStatusFromTry;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final AktorOppslagClient aktorOppslagClient;

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final UnleashService unleashService;

    private final TaskProcessorService taskProcessorService;

    private final TaskRepository taskRepository;

    private final JdbcTemplate jdbcTemplate;

    public void behandleOppfolgingStartet(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        if (!unleashService.isKafkaEnabled()) {
            log.info("Kafka toggle is not enabled, skipping processing of record for aktorId={}", oppfolgingStartetKafkaDTO.getAktorId());
            return;
        }

        /*
            Siden vi utfører oppgaver som ikke er idempotent før vi lagrer resultatet i databasen, så gjør vi en ekstra sjekk
            på om koblingen til databasen er grei, slik at vi ikke utfører oppgaver og ikke får lagret resultatet.
        */
        if (DbUtils.checkDbHealth(jdbcTemplate).isUnhealthy()) {
            log.error("Health check failed, aborting consumption of kafka record");
            throw new IllegalStateException("Cannot connect to database");
        }

        AktorId aktorId = oppfolgingStartetKafkaDTO.getAktorId();
        Fnr fnr = aktorOppslagClient.hentFnr(aktorId);

        List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);

        BrukerRegistreringWrapper brukerRegistrering = veilarbregistreringClient.hentRegistrering(fnr)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        LocalDateTime registreringsdato = RegistreringUtils.hentRegistreringDato(brukerRegistrering);

        if (!RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder)) {
            log.info("Bruker {} er ikke nylig registrert og skal ikke ha aktivitet/dialog", aktorId);
            return;
        }

        Oppfolgingsperiode gjeldendeOppfolgingsperiode = OppfolgingsperiodeUtils.hentGjeldendeOppfolgingsperiode(oppfolgingsperioder)
                .orElseThrow(() -> new IllegalStateException("Bruker har ikke gjeldende oppfølgingsperiode"));

        String oppfolgingsperiodeId = gjeldendeOppfolgingsperiode.getUuid().toString();

        boolean erNyRegistrert = RegistreringUtils.erNyregistrert(brukerRegistrering);
        boolean erNySykmeldtBrukerRegistrert = RegistreringUtils.erNySykmeldtBrukerRegistrert(brukerRegistrering);

        List<Task> tasksToPerform = new ArrayList<>();

        if (erNyRegistrert) {
            Optional<Task> maybePermittertDialogTask = createTaskIfNotStoredInDb(
                    () -> lagKanskjePermittertDialogTask(oppfolgingsperiodeId, aktorId), taskRepository
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
                    () -> lagCvJobbprofilAktivitetTask(oppfolgingsperiodeId, aktorId), taskRepository
            );

            Optional<Task> maybeJobbsokerkompetanseAktivitetTask = createTaskIfNotStoredInDb(
                    () -> lagJobbsokerkompetanseAktivitetTask(oppfolgingsperiodeId, aktorId), taskRepository
            );


            if (maybeCvJobbprofilAktivitetTask.isPresent()) {
                Task cvJobbprofilAktivitetTask = maybeCvJobbprofilAktivitetTask.get();

                Try<String> cvJobbprofilAktivitetResult = taskProcessorService.processOpprettAktivitetTask(cvJobbprofilAktivitetTask);
                cvJobbprofilAktivitetTask.setTaskStatus(getStatusFromTry(cvJobbprofilAktivitetResult));

                tasksToPerform.add(cvJobbprofilAktivitetTask);
            }

            if (maybeJobbsokerkompetanseAktivitetTask.isPresent()) {
                Task jobbsokerkompetanseAktivitetTask = maybeJobbsokerkompetanseAktivitetTask.get();

                Try<String> jobbsokerkompetanseAktivitetResult = taskProcessorService.processOpprettAktivitetTask(jobbsokerkompetanseAktivitetTask);
                jobbsokerkompetanseAktivitetTask.setTaskStatus(getStatusFromTry(jobbsokerkompetanseAktivitetResult));

                tasksToPerform.add(jobbsokerkompetanseAktivitetTask);
            }
        }

        taskRepository.insert(tasksToPerform);

        log.info("Finished consuming kafka record for aktorId={}", aktorId);
    }

}
