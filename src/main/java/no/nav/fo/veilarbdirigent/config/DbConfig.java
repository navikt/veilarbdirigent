package no.nav.fo.veilarbdirigent.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.sql.SqlUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.sql.DataSource;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@EnableTransactionManagement
public class DbConfig {

    public static final String VEILARBDIRIGENTDB_URL = "VEILARBDIRIGENTDB_URL";
    public static final String VEILARBDIRIGENTDB_USERNAME = "VEILARBDIRIGENTDB_USERNAME";
    public static final String VEILARBDIRIGENTDB_PASSWORD = "VEILARBDIRIGENTDB_PASSWORD";

    @Bean
    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getRequiredProperty(VEILARBDIRIGENTDB_URL));
        config.setUsername(getRequiredProperty(VEILARBDIRIGENTDB_USERNAME));
        config.setPassword(getRequiredProperty(VEILARBDIRIGENTDB_PASSWORD));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        return new HikariDataSource(config);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource datasource) throws NamingException {
        return new JdbcTemplate(datasource);
    }

    @Bean
    public Pingable dbPinger(final DataSource ds) {
        HelsesjekkMetadata metadata = new HelsesjekkMetadata("db",
                "Database: " + getRequiredProperty(VEILARBDIRIGENTDB_URL),
                "Database for veilarbdirigent",
                true);

        return () -> {
            try {
                SqlUtils.select(ds, "dual", resultSet -> resultSet.getInt(1))
                        .column("count(1)")
                        .execute();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }
}
