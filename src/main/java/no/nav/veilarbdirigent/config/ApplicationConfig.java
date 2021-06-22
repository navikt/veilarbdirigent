package no.nav.veilarbdirigent.config;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.CachedAktorOppslagClient;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.AktorregisterHttpClient;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.featuretoggle.UnleashClientImpl;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.job.leader_election.ShedLockLeaderElectionClient;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.sts.OpenAmSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.Credentials;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.common.utils.NaisUtils.getCredentials;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    public static final String APPLICATION_NAME = "veilarbdirigent";

    @Bean
    public Credentials serviceUserCredentials() {
        return getCredentials("service_user");
    }

    // TODO: Bedre å bruke NaisSystemUserTokenProvider hvis alle tjenester støtter det
    @Bean
    public SystemUserTokenProvider systemUserTokenProvider(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        Credentials isso = new Credentials(properties.getOpenAmUsername(), properties.getOpenAmPassword());
        return new OpenAmSystemUserTokenProvider(properties.getOpenAmDiscoveryUrl(), properties.getOpenAmRedirectUrl(), isso, serviceUserCredentials);
    }

    @Bean
    public AktorOppslagClient aktorOppslagClient(EnvironmentProperties properties, SystemUserTokenProvider tokenProvider) {
        AktorregisterClient aktorregisterClient = new AktorregisterHttpClient(
                properties.getAktorregisterUrl(), APPLICATION_NAME, tokenProvider::getSystemUserToken
        );
        return new CachedAktorOppslagClient(aktorregisterClient);
    }

    @Bean
    public MetricsClient metricsClient() {
        return new InfluxClient();
    }

    @Bean
    public LeaderElectionClient leaderElectionClient(LockProvider lockProvider) {
        return new ShedLockLeaderElectionClient(lockProvider);
    }

    @Bean
    public UnleashClient unleashClient(EnvironmentProperties properties) {
        return new UnleashClientImpl(properties.getUnleashUrl(), APPLICATION_NAME);
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
