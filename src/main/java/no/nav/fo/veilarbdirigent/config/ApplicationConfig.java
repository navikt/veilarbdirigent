package no.nav.fo.veilarbdirigent.config;

import io.vavr.collection.List;
import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.fo.veilarbdirigent.core.Actuator;
import no.nav.fo.veilarbdirigent.core.CoreIn;
import no.nav.fo.veilarbdirigent.core.CoreOut;
import no.nav.fo.veilarbdirigent.core.MessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static no.nav.apiapp.ApiApplication.Sone.FSS;

@Configuration
@Import({MessageHandlerConfig.class, ActuatorConfig.class})
public class ApplicationConfig implements ApiApplication.NaisApiApplication {
    public static final String APPLICATION_NAME = "veilarbdirigent";

    @Override
    public Sone getSone() {
        return FSS;
    }

    @Override
    public boolean brukSTSHelsesjekk() {
        return false;
    }

    @Override
    public String getApplicationName() {
        return APPLICATION_NAME;
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
    }

    @Bean
    public CoreOut coreOut(List<Actuator> actuators) {
        return new CoreOut(actuators);
    }

    @Bean
    public CoreIn coreIn(CoreOut coreOut, List<MessageHandler> handlers) {
        return new CoreIn(coreOut, handlers);
    }
}