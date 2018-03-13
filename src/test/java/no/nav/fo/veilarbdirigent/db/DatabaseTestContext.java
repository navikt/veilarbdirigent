package no.nav.fo.veilarbdirigent.db;

import lombok.val;
import no.nav.dialogarena.config.fasit.DbCredentials;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.TestEnvironment;
import org.eclipse.jetty.plus.jndi.Resource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.naming.NamingException;
import java.util.Optional;

import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.APPLICATION_NAME;

public class DatabaseTestContext {

    public static void setupContext(String miljo) throws NamingException {
        val dbCredential = Optional.ofNullable(miljo)
                .map(TestEnvironment::valueOf)
                .map(testEnvironment -> FasitUtils.getDbCredentials(testEnvironment, APPLICATION_NAME));

        if (dbCredential.isPresent()) {
            setDataSourceProperties(dbCredential.get());
        } else {
            setInMemoryDataSourceProperties();
        }

    }

    public static void setupInMemoryContext() throws NamingException {
        setupContext(null);
    }

    private static void setDataSourceProperties(DbCredentials dbCredentials) throws NamingException {
        new Resource("jdbc/veilarbdirigentDB", setupDatasource(
                dbCredentials.url,
                dbCredentials.username,
                dbCredentials.password
        ));
    }

    private static void setInMemoryDataSourceProperties() throws NamingException {
        new Resource("jdbc/veilarbdirigentDB", setupDatasource(
                "jdbc:h2:mem:veilarbdirigent;DB_CLOSE_DELAY=-1;MODE=Oracle",
                "sa",
                ""
        ));
    }

    private static SingleConnectionDataSource setupDatasource(String url, String username, String password) {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setSuppressClose(true);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);

        return ds;
    }
}
