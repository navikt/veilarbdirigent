package no.nav.veilarbdirigent.input.rest;

import no.nav.veilarbdirigent.core.Core;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestConfig {

    @Bean
    public BusyController busyController(Core core) {
        return new BusyController(core);

    }

}
