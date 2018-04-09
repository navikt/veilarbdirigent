package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.handlers.BusyHandler;
import no.nav.fo.veilarbdirigent.handlers.OppfolgingsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfig {
    @Bean
    public OppfolgingsHandler oppfolgingsHandler() {
        return new OppfolgingsHandler();
    }

    @Bean
    public BusyHandler busyHandler() {
        return new BusyHandler();
    }
}
