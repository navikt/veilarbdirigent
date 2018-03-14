package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.coreapi.Actuator;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

public class ActuatorTestConfig {
    @Bean
    public Actuator actuator() {
        return mock(Actuator.class);
    }
}
