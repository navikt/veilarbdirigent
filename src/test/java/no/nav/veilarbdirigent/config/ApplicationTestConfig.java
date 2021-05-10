package no.nav.veilarbdirigent.config;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.Credentials;
import no.nav.veilarbdirigent.controller.AdminController;
import no.nav.veilarbdirigent.mock.LocalH2Database;
import no.nav.veilarbdirigent.mock.MetricsClientMock;
import no.nav.veilarbdirigent.repository.TaskRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

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
    public Credentials serviceUserCredentials() {
        return new Credentials("username", "password");
    }

    @Bean
    public AktorOppslagClient aktorOppslagClient() {
        return new AktorOppslagClient() {
            @Override
            public Fnr hentFnr(AktorId aktorId) {
                return null;
            }

            @Override
            public AktorId hentAktorId(Fnr fnr) {
                return null;
            }

            @Override
            public Map<AktorId, Fnr> hentFnrBolk(List<AktorId> list) {
                return null;
            }

            @Override
            public Map<Fnr, AktorId> hentAktorIdBolk(List<Fnr> list) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return null;
            }
        };
    }

    @Bean
    public MetricsClient metricsClient() {
        return new MetricsClientMock();
    }

    @Bean
    public TaskRepository taskDAO(JdbcTemplate jdbcTemplate) {
        return new TaskRepository(jdbcTemplate);
    }

    @Bean
    public LeaderElectionClient leaderElectionClient() {
        var client = mock(LeaderElectionClient.class);
        when(client.isLeader()).thenAnswer(a -> true);
        return client;
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
