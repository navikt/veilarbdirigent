package no.nav.veilarbdirigent.config;

import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.utils.Credentials;
import no.nav.veilarbdirigent.admin.AdminController;
import no.nav.veilarbdirigent.mock.AktorregisterClientMock;
import no.nav.veilarbdirigent.mock.LocalH2Database;
import no.nav.veilarbdirigent.mock.MetricsClientMock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@EnableConfigurationProperties({EnvironmentProperties.class})
@Import({
        AdminController.class,
        DAOConfig.class,
        CoreConfig.class,
        FilterTestConfig.class
})
public class ApplicationTestConfig {

    @Bean
    public Credentials serviceUserCredentials() {
        return new Credentials("username", "password");
    }

    @Bean
    public AktorregisterClient aktorregisterClient() {
        return new AktorregisterClientMock();
    }

    @Bean
    public MetricsClient metricsClient() {
        return new MetricsClientMock();
    }

    @Bean
    public LeaderElectionClient leaderElectionClient() {
        var client = mock(LeaderElectionClient.class);
        when(client.isLeader()).thenAnswer(a -> true);
        return client;
    }

    @Bean
    public Transactor transactor(PlatformTransactionManager transactionManager) {
        return new Transactor(transactionManager);
    }

    @Bean
    public DataSource dataSource() {
        return LocalH2Database.getDb().getDataSource();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return LocalH2Database.getDb();
    }

}
