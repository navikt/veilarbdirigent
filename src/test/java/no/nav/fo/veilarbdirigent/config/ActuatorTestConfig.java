package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.core.Actuator;
import no.nav.fo.veilarbdirigent.core.Core;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

public class ActuatorTestConfig {
    @Bean
    public Actuator actuator(Core core) {
        Actuator mock = mock(Actuator.class);
        core.registerActuator(TestUtils.TASK_TYPE, mock);
        return mock;
    }
}
