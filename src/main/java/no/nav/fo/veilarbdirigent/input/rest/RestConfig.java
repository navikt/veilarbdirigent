package no.nav.fo.veilarbdirigent.input.rest;

import no.nav.fo.veilarbdirigent.core.Core;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestConfig {

    @Bean
    public BusyController busyController(Core core) {
        return new BusyController(core);

    }

}
