package no.nav.veilarbdirigent.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.veilarbdirigent.controller.AdminController;
import no.nav.veilarbdirigent.mock.MetricsClientMock;
import no.nav.veilarbdirigent.repository.TaskRepository;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@EnableConfigurationProperties({EnvironmentProperties.class})
@Import({
        AdminController.class,
        FilterTestConfig.class
})
public class ApplicationTestConfig {

    @Bean
    public AktorOppslagClient aktorOppslagClient() {
        return Mockito.mock(AktorOppslagClient.class);
    }

    @Bean
    public MetricsClient metricsClient() {
        return new MetricsClientMock();
    }

    @Bean
    public TaskRepository taskDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        return new TaskRepository(jdbcTemplate);
    }

    @Bean
    public LeaderElectionClient leaderElectionClient() {
        var client = mock(LeaderElectionClient.class);
        when(client.isLeader()).thenAnswer(a -> true);
        return client;
    }

    @Bean
    public DataSource dataSource() throws IOException {
        var db = EmbeddedPostgres.start();
        return db.getPostgresDatabase();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
