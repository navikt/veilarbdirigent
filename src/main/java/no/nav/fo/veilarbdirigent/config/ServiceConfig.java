package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.service.aktivitet.VeilarbaktivitetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public VeilarbaktivitetService actuator() {
        return new VeilarbaktivitetService();
    }
}
