package no.nav.fo.veilarbdirigent.config;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.jdbc.Transactor;
import no.nav.sbl.sql.SQLFunction;
import no.nav.sbl.sql.SqlUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
public class DbConfig {
    @Bean
    public DataSource dataSourceJndiLookup() throws NamingException {
        JndiDataSourceLookup jndiDataSourceLookup = new JndiDataSourceLookup();
        jndiDataSourceLookup.setResourceRef(true);
        return jndiDataSourceLookup.getDataSource("jdbc/veilarbdirigentDB");
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
    public Transactor transactor(PlatformTransactionManager platformTransactionManager) {
        return new Transactor(platformTransactionManager);
    }

    @Bean
    public Pingable dbPinger(final DataSource ds) {
        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "veilarboppfolgingDB: " + System.getProperty("veilarboppfolgingDB.url"),
                "Enkel spørring mot Databasen for VeilArbOppfolging.",
                true
        );

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
