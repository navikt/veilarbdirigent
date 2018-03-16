package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Core;

public class OppfolgingFeedService {

    private final FeedDAO feedDAO;
    private final Core core;

    public OppfolgingFeedService(Core core, FeedDAO feedDAO) {
        this.core = core;
        this.feedDAO = feedDAO;
    }

    long sisteKjenteId() {
        return feedDAO.sisteKjenteId();
    }

    void compute(String lastEntryId, List<OppfolgingDataFraFeed> elements) {
        elements.forEach((element) -> {
            core.submit(element);
            feedDAO.oppdaterSisteKjenteId(element.getId());
        });
    }
}
