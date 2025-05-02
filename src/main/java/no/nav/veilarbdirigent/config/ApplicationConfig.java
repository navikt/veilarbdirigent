package no.nav.veilarbdirigent.config;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.CachedAktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.job.leader_election.ShedLockLeaderElectionClient;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.token_client.client.MachineToMachineTokenClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

import static no.nav.common.utils.EnvironmentUtils.isProduction;
import static no.nav.common.utils.UrlUtils.createServiceUrl;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    public static final String APPLICATION_NAME = "veilarbdirigent";

    @Bean
    public AzureAdMachineToMachineTokenClient azureAdMachineToMachineTokenClient() {
        return AzureAdTokenClientBuilder.builder()
                .withNaisDefaults()
                .buildMachineToMachineTokenClient();
    }

    @Bean
    public AktorOppslagClient aktorregisterClient(MachineToMachineTokenClient tokenClient) {
        String tokenScop = String.format("api://%s-fss.pdl.pdl-api/.default",
                isProduction().orElse(false) ? "prod" : "dev"
        );
        return new CachedAktorOppslagClient(new PdlAktorOppslagClient(
                createServiceUrl("pdl-api", "pdl", false),
                () -> tokenClient.createMachineToMachineToken(tokenScop))
        );
    }

    @Bean
    public MetricsClient metricsClient() {
        return new MetricsClient() {
            @Override
            public void report(Event event) {
                // TODO: Implement metrics reporting
            }

            @Override
            public void report(String s, Map<String, Object> map, Map<String, String> map1, long l) {
                // TODO: Implement metrics reporting
            }
        };
    }

    @Bean
    public LeaderElectionClient leaderElectionClient(LockProvider lockProvider) {
        return new ShedLockLeaderElectionClient(lockProvider);
    }

    @Bean
    public LockProvider lockProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
                        .usingDbTime()
                        .build()
        );
    }

}
