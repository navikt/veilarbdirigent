package no.nav.fo.veilarbdirigent.input.feed;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingFeedServiceConfig.class,
        OppfolgingFeedConsumerConfig.class
})
public class FeedConfig {
}
