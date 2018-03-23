package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.sbl.jdbc.Transactor;

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
            core.submit(element);
            feedDAO.oppdaterSisteKjenteId(element.getId());
        }));
    }
}
