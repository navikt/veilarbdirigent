package no.nav.veilarbdirigent.feed.consumer;

import no.nav.veilarbdirigent.feed.OppfolgingDataFraFeed;

import java.util.List;

@FunctionalInterface
public interface FeedCallback {
    void call(String lastEntryId, List<OppfolgingDataFraFeed> data);
}
