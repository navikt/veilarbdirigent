package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.handlers.OppfolgingsHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfig {
    public OppfolgingsHandler oppfolgingsHandler() {
        return new OppfolgingsHandler();
    }
}
