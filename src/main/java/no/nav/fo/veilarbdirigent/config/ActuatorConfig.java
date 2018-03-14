package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.core.Actuator;
import no.nav.fo.veilarbdirigent.core.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    @Bean
    public Actuator actuator() {
        return new Actuator() {

            @Override
            public Task handle(Task task) {
                return null;
            }
        };
    }
}
