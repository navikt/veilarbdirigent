package no.nav.fo.veilarbdirigent.config.databasecleanup;

import org.junit.jupiter.api.AfterEach;

public interface FeedMetadataCleanup extends Cleanup {

    @AfterEach
    default void deleteFeedMetadata() {
        getJdbc().update("UPDATE FEED_METADATA SET NYE_BRUKERE_FEED_SISTE_ID = 0");
    }
}
