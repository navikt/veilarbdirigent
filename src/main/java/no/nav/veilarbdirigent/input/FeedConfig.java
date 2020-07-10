package no.nav.veilarbdirigent.input;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingFeedServiceConfig.class,
        OppfolgingFeedConsumerConfig.class
})
public class FeedConfig {
}
