package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.handlers.OppfolgingsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfig {
    @Bean
    public OppfolgingsHandler oppfolgingsHandler() {
        return new OppfolgingsHandler();
    }
}
