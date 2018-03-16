package no.nav.fo.veilarbdirigent.db;

import io.vavr.control.Option;
import no.nav.dialogarena.config.fasit.DbCredentials;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.TestEnvironment;

import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.APPLICATION_NAME;
import static no.nav.fo.veilarbdirigent.config.DbConfig.*;

public class DatabaseTestContext {
    public static void setupContext(String miljo) {
        Option<DbCredentials> dbCredential = Option.of(miljo)
                .map(TestEnvironment::valueOf)
                .map(testEnvironment -> FasitUtils.getDbCredentials(testEnvironment, APPLICATION_NAME));

        if (dbCredential.isDefined()) {
            dbCredential.forEach(DatabaseTestContext::setDataSourceProperties);
        } else {
            setInMemoryDataSourceProperties();
        }

    }

    public static void setupInMemoryContext() {
        setupContext(null);
    }

    private static void setDataSourceProperties(DbCredentials dbCredentials) {
        System.setProperty(VEILARBDIRIGENTDB_URL, dbCredentials.url);
        System.setProperty(VEILARBDIRIGENTDB_USERNAME, dbCredentials.getUsername());
        System.setProperty(VEILARBDIRIGENTDB_PASSWORD, dbCredentials.getPassword());
    }

    private static void setInMemoryDataSourceProperties() {
        System.setProperty(VEILARBDIRIGENTDB_URL, "jdbc:h2:mem:veilarbdirigent;DB_CLOSE_DELAY=-1;MODE=Oracle");
        System.setProperty(VEILARBDIRIGENTDB_USERNAME, "sa");
        System.setProperty(VEILARBDIRIGENTDB_PASSWORD, "password");
    }
}
