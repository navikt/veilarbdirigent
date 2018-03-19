package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.coreapi.Actuator;
import no.nav.fo.veilarbdirigent.coreapi.Task;
import no.nav.fo.veilarbdirigent.coreapi.TaskType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    @Bean
    public Actuator actuator() {
        return new Actuator<String, String>() {

            @Override
            public Task<String, String> handle(Task<String, String> task) {
                return null;
            }

            @Override
            public TaskType getType() {
                return new TaskType("String.class");
            }
        };
    }
}
