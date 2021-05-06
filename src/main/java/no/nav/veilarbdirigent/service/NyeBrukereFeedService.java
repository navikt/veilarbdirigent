package no.nav.veilarbdirigent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.veilarbdirigent.core.api.Task;
import no.nav.veilarbdirigent.core.api.TaskType;
import no.nav.veilarbdirigent.feed.OppfolgingDataFraFeed;
import no.nav.veilarbdirigent.repository.FeedDAO;
import no.nav.veilarbdirigent.utils.TypedField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NyeBrukereFeedService {

    private final FeedDAO feedDAO;

    private final LeaderElectionClient leaderElectionClient;

    private final TransactionTemplate transactor;

    public long sisteKjenteId() {
        return feedDAO.sisteKjenteId();
    }

    public void processFeedElements(List<OppfolgingDataFraFeed> elements) {
        if (!leaderElectionClient.isLeader()) {
            log.warn("Is not leader, Skipping action");
            return;
        }

        elements.forEach((element) -> {
            /*
                We cannot try to execute tasks if they are already stored.
                We either need to store all, or check if the task already exists.

                Try to perform all the tasks. The ones who are completed can be stored with success status.
                The ones who could not be completed should be stored with failed status.
            */

        });

        // TODO: Lagre i database

//        elements.forEach((element) -> {
//            runInMappedDiagnosticContext("batchID", String.valueOf(element.id), () -> submitToCore(element));
//            core.forceScheduled();
//        });
    }

    private void storeTasks(OppfolgingDataFraFeed element) {
        transactor.executeWithoutResult((status) -> {
            long elementId = element.getId();

            log.info("Submitting feed message with id: {}", elementId);

            TaskType AKTIVITET_TYPE = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

            TaskType DIALOG_TYPE = TaskType.of("OPPFOLGING_OPPRETT_DIALOG");

            String permitertDialog = "kanskje_permitert_dialog";

            Task cvJobbprofilAktivitet = new Task<>()
                    .withId(elementId + "cv_jobbprofil_aktivitet")
                    .withType(AKTIVITET_TYPE)
                    .withData(new TypedField<>(new AktivitetHandler.OppfolgingDataMedMal(element, "cv_jobbprofil_aktivitet")));

            Task jobbsokerkompetanseAktivitet = new Task<>()
                    .withId(elementId + "jobbsokerkompetanse")
                    .withType(AKTIVITET_TYPE)
                    .withData(new TypedField<>(new AktivitetHandler.OppfolgingDataMedMal(element, "jobbsokerkompetanse_aktivitet")));

            Task dialogTask = new Task<>()
                    .withId(elementId + "kanskjePermitert")
                    .withType(DIALOG_TYPE)
                    .withData(new TypedField<>(new DialogHandler.OppfolgingData(element, permitertDialog)));

            feedDAO.oppdaterSisteKjenteId(elementId);
            log.info("Feed message with id: {} completed successfully", elementId);
        });
    }

}
