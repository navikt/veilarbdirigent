package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OppfolgingFeedConfig.class)
public class FeedConfig {
}
