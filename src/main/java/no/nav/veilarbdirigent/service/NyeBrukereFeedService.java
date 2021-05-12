package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.feed.OppfolgingDataFraFeed;
import no.nav.veilarbdirigent.repository.FeedRepository;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.veilarbdirigent.utils.TaskFactory.*;
import static no.nav.veilarbdirigent.utils.TaskUtils.createTaskIfNotStoredInDb;
import static no.nav.veilarbdirigent.utils.TaskUtils.getStatusFromTry;

@Slf4j
@Service
@RequiredArgsConstructor
public class NyeBrukereFeedService {

    private final TaskProcessorService taskProcessorService;

    private final TaskRepository taskRepository;

    private final FeedRepository feedRepository;

    private final LeaderElectionClient leaderElectionClient;

    private final TransactionTemplate transactor;

    private final UnleashService unleashService;

    public long sisteKjenteId() {
        return feedRepository.sisteKjenteId();
    }

    public void processFeedElements(List<OppfolgingDataFraFeed> elements) {
        if (unleashService.isKafkaEnabled()) {
            log.info("Stopping processing of feed elements since kafka toggle is enabled");
            return;
        }

        if (!leaderElectionClient.isLeader()) {
            log.warn("Is not leader, Skipping action");
            return;
        }

        elements.forEach((element) -> {
            String elementIdStr = String.valueOf(element.getId());
            AktorId aktorId = AktorId.of(element.getAktorId());

            log.info("Submitting feed message with id: {}", elementIdStr);

            List<Task> tasksToPerform = new ArrayList<>();

            boolean erNyRegistrert = RegistreringUtils.erNyregistrert(element.getForeslattInnsatsgruppe());
            boolean erNySykmeldtBrukerRegistrert = RegistreringUtils.erNySykmeldtBrukerRegistrert(element.getSykmeldtBrukerType());

            log.info("Processing feed element. aktorId={} erNyRegistrert={} erNySykmeldtBrukerRegistrert={}", aktorId, erNyRegistrert, erNySykmeldtBrukerRegistrert);

            if (erNyRegistrert) {
                Optional<Task> maybePermittertDialogTask = createTaskIfNotStoredInDb(
                        () -> lagKanskjePermittertDialogTask(elementIdStr, aktorId), taskRepository
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
                        () -> lagCvJobbprofilAktivitetTask(elementIdStr, aktorId), taskRepository
                );

                Optional<Task> maybeJobbsokerkompetanseAktivitetTask = createTaskIfNotStoredInDb(
                        () -> lagJobbsokerkompetanseAktivitetTask(elementIdStr, aktorId), taskRepository
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

            transactor.executeWithoutResult((status) -> {
                taskRepository.insert(tasksToPerform);

                feedRepository.oppdaterSisteKjenteId(element.getId());

                log.info("Feed message with id: {} completed successfully", elementIdStr);
            });
        });
    }

}

