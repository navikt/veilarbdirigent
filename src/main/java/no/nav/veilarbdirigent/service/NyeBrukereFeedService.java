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
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import no.nav.veilarbdirigent.utils.TaskFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

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

    public long sisteKjenteId() {
        return feedRepository.sisteKjenteId();
    }

    public void processFeedElements(List<OppfolgingDataFraFeed> elements) {
        if (!leaderElectionClient.isLeader()) {
            log.warn("Is not leader, Skipping action");
            return;
        }

        elements.forEach((element) -> {
            long elementId = element.getId();
            AktorId aktorId = AktorId.of(element.getAktorId());

            log.info("Submitting feed message with id: {}", elementId);

            List<Task> tasksToPerform = new ArrayList<>();

            boolean erNyRegistrert = RegistreringUtils.erNyregistrert(element.getForeslattInnsatsgruppe());
            boolean erNySykmeldtBrukerRegistrert = RegistreringUtils.erNySykmeldtBrukerRegistrert(element.getSykmeldtBrukerType());

            log.info("Processing feed element. aktorId={} erNyRegistrert={} erNySykmeldtBrukerRegistrert={}", aktorId, erNyRegistrert, erNySykmeldtBrukerRegistrert);

            if (erNyRegistrert) {
                Task kanskjePermittertDialogTask = TaskFactory.lagKanskjePermittertDialogTask(elementId, aktorId);

                Try<String> dialogTaskResult = taskProcessorService.processOpprettDialogTask(kanskjePermittertDialogTask);
                kanskjePermittertDialogTask.setTaskStatus(getStatusFromTry(dialogTaskResult));

                tasksToPerform.add(kanskjePermittertDialogTask);
            }

            if (erNySykmeldtBrukerRegistrert || erNyRegistrert) {
                Task cvJobbprofilAktivitetTask = TaskFactory.lagCvJobbprofilAktivitetTask(elementId, aktorId);

                Task jobbsokerkompetanseAktivitetTask = TaskFactory.lagJobbsokerkompetanseAktivitetTask(elementId, aktorId);

                Try<String> cvJobbprofilAktivitetResult = taskProcessorService.processOpprettAktivitetTask(cvJobbprofilAktivitetTask);

                if (cvJobbprofilAktivitetResult.isSuccess()) {
                    Try<String> jobbsokerkompetanseAktivitetResult = taskProcessorService.processOpprettAktivitetTask(cvJobbprofilAktivitetTask);

                    cvJobbprofilAktivitetTask.setTaskStatus(TaskStatus.OK);
                    jobbsokerkompetanseAktivitetTask.setTaskStatus(getStatusFromTry(jobbsokerkompetanseAktivitetResult));
                } else {
                    // Hvis vi ikke får opprettet cv/jobbprofil aktivitet så dropper vi å prøve å opprette jobbsokerkompetanse
                    cvJobbprofilAktivitetTask.setTaskStatus(TaskStatus.FAILED);
                    jobbsokerkompetanseAktivitetTask.setTaskStatus(TaskStatus.PENDING);
                }

                tasksToPerform.addAll(List.of(cvJobbprofilAktivitetTask, jobbsokerkompetanseAktivitetTask));
            }

            transactor.executeWithoutResult((status) -> {
                taskRepository.insert(tasksToPerform);

                feedRepository.oppdaterSisteKjenteId(elementId);

                log.info("Feed message with id: {} completed successfully", elementId);
            });
        });
    }

}

