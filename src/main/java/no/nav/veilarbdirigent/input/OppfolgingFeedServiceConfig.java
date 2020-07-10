package no.nav.veilarbdirigent.input;

import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.veilarbdirigent.config.Transactor;
import no.nav.veilarbdirigent.core.Core;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OppfolgingFeedServiceConfig {
    @Bean
    public FeedDAO feedDAO(JdbcTemplate jdbc) {
        return new FeedDAO(jdbc);
    }

    @Bean
    public OppfolgingFeedService oppfolgingFeedService(Core core, FeedDAO dao, Transactor transactor, LeaderElectionClient leaderElectionClient) {
        return new OppfolgingFeedService(core, dao, transactor, leaderElectionClient);
    }
}
