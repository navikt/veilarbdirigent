package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.CoreIn;

public class OppfolgingFeedService {

    private final FeedDAO feedDAO;
    private final CoreIn core;

    public OppfolgingFeedService(CoreIn core, FeedDAO feedDAO) {
        this.core = core;
        this.feedDAO = feedDAO;
    }

    long sisteKjenteId() {
        return feedDAO.sisteKjenteId();
    }

    void compute(String lastEntryId, List<OppfolgingDataFraFeed> elements) {
        elements.map(element -> {
            core.submit(element);
            return element.getId();
        })
                .lastOption()
                .forEach(feedDAO::oppdaterSisteKjenteId);
    }
}
