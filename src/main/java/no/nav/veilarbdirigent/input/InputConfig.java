package no.nav.veilarbdirigent.input;

import no.nav.veilarbdirigent.input.rest.RestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        FeedConfig.class,
        RestConfig.class
})
public class InputConfig {
}
