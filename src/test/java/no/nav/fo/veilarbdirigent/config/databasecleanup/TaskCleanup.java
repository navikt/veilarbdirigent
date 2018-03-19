package no.nav.fo.veilarbdirigent.config.databasecleanup;

import org.junit.jupiter.api.AfterEach;

public interface TaskCleanup extends Cleanup {

    @AfterEach
    default void deleteTestData() {
        getJdbc().update("DELETE FROM TASK");
    }
}
