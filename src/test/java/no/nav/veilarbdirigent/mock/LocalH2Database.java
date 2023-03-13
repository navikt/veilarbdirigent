package no.nav.veilarbdirigent.mock;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Properties;

public class LocalH2Database {

    private static JdbcTemplate db;

    public static JdbcTemplate getDb() {
        if (db == null) {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:file:~/database/veilarbdirigent;DB_CLOSE_DELAY=-1;MODE=Oracle;AUTO_SERVER=TRUE;");
       //     dataSource.setURL("jdbc:h2:mem:veilarbdirigent-local;DB_CLOSE_DELAY=-1;MODE=Oracle");
            dataSource.setUser("sa");
            dataSource.setPassword("sa");

            db = new JdbcTemplate(dataSource);
            initDb(dataSource);
        }


        return db;
    }

    private static void initDb(DataSource dataSource) {
        var properties = new Properties();
        properties.put("flyway.cleanDisabled", false);
        var flyway = Flyway
                .configure()
                .configuration(properties)
                .table("schema_version")
                .dataSource(dataSource)
                .load();
        // flyway.clean();
        // flyway.repair();
        flyway.migrate();
    }

}
