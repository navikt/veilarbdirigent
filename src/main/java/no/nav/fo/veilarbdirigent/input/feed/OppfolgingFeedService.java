package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.sbl.jdbc.Transactor;
import org.slf4j.MDC;

@Slf4j
public class OppfolgingFeedService {

    private final FeedDAO feedDAO;
    private final Core core;
    private final Transactor transactor;

    public OppfolgingFeedService(Core core, FeedDAO feedDAO, Transactor transactor) {
        this.core = core;
        this.feedDAO = feedDAO;
        this.transactor = transactor;
    }

    long sisteKjenteId() {
        return feedDAO.sisteKjenteId();
    }

    void compute(String lastEntryId, List<OppfolgingDataFraFeed> elements) {
        elements.forEach((element) -> {
            submitToCore(element);
            core.forceScheduled();
        });
    }

    private void submitToCore(OppfolgingDataFraFeed element) {
        MDC.put("batchID", String.valueOf(element.id));
        transactor.inTransaction(() -> {
            log.info("Submitting feed message with id: {}", element.id);
            core.submitInTransaction(element);
            feedDAO.oppdaterSisteKjenteId(element.getId());
            log.info("Feed message with id: {} completed successfully", element.id);
        });
        MDC.remove("batchID");
    }
}
