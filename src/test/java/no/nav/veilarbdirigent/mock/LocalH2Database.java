package no.nav.veilarbdirigent.mock;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class LocalH2Database {

    private static JdbcTemplate db;

    public static JdbcTemplate getDb() {
        if (db == null) {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:veilarbdirigent-local;DB_CLOSE_DELAY=-1;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=3");
            dataSource.setUser("user");

            db = new JdbcTemplate(dataSource);
            initDb(dataSource);
        }


        return db;
    }

    private static void initDb(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();
    }

}
