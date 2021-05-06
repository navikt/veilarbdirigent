package no.nav.veilarbdirigent.feed;

import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.veilarbdirigent.config.Transactor;
import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.repository.FeedDAO;

import static no.nav.veilarbdirigent.core.Utils.runInMappedDiagnosticContext;

@Slf4j
public class OppfolgingFeedService {

    private final FeedDAO feedDAO;
    private final Core core;
    private final Transactor transactor;
    private final LeaderElectionClient leaderElectionClient;

    public OppfolgingFeedService(Core core, FeedDAO feedDAO, Transactor transactor, LeaderElectionClient leaderElectionClient) {
        this.core = core;
        this.feedDAO = feedDAO;
        this.transactor = transactor;
        this.leaderElectionClient = leaderElectionClient;
    }

    public long sisteKjenteId() {
        return feedDAO.sisteKjenteId();
    }

    public void compute(String lastEntryId, List<OppfolgingDataFraFeed> elements) {
        if (!leaderElectionClient.isLeader()){
            log.warn("Is not leader, Skipping action");
            return;
        }
        elements.forEach((element) -> {
            runInMappedDiagnosticContext("batchID", String.valueOf(element.id), () -> submitToCore(element));
            core.forceScheduled();
        });
    }

    private void submitToCore(OppfolgingDataFraFeed element) {
        transactor.inTransaction(() -> {
            log.info("Submitting feed message with id: {}", element.id);
            core.submitInTransaction(element);
            feedDAO.oppdaterSisteKjenteId(element.getId());
            log.info("Feed message with id: {} completed successfully", element.id);
        });
    }
}
