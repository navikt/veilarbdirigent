package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConsumerConfig;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedServiceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingFeedServiceConfig.class,
        OppfolgingFeedConsumerConfig.class
})
public class FeedConfig {
}
