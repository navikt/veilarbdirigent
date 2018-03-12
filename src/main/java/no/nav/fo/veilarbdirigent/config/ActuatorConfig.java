package no.nav.fo.veilarbdirigent.config;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Actuator;
import org.springframework.context.annotation.Bean;

public class ActuatorConfig {

    @Bean
    // Må være her for å slippe konvertering fra java.util.List til vavr overalt
    public List<Actuator> all(java.util.List<Actuator> handlers) {
        return List.ofAll(handlers);
    }
}
