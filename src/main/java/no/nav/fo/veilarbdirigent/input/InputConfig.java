package no.nav.fo.veilarbdirigent.input;

import no.nav.fo.veilarbdirigent.input.feed.FeedConfig;
import no.nav.fo.veilarbdirigent.input.rest.RestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        FeedConfig.class,
        RestConfig.class
})
public class InputConfig {
}
