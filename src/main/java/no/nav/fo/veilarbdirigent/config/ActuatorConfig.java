package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.core.Actuator;
import no.nav.fo.veilarbdirigent.core.Task;
import org.springframework.context.annotation.Bean;

public class ActuatorConfig {

    @Bean
    public Actuator actuator() {
        return new Actuator() {

            @Override
            public void handle(Task task) {

            }
        };
    }
}
