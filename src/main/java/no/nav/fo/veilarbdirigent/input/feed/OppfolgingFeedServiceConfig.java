package no.nav.fo.veilarbdirigent.input.feed;

import no.nav.fo.veilarbdirigent.core.Core;
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
    public OppfolgingFeedService oppfolgingFeedService(Core core, FeedDAO dao) {
        return new OppfolgingFeedService(core, dao);
    }
}
