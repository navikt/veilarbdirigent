package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.sbl.jdbc.Transactor;

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
        elements.forEach((element) -> transactor.inTransaction(() -> {
            log.info("Submitting feed message with id: {}", element.id);
            core.submit(element);
            feedDAO.oppdaterSisteKjenteId(element.getId());
        }));
    }
}
