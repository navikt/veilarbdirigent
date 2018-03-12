package no.nav.fo.veilarbdirigent;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static no.nav.apiapp.ApiApplication.Sone.FSS;

@Configuration
@Import({
        DemoRessurs.class
})
public class ApplicationConfig implements ApiApplication.NaisApiApplication {

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

    public static final String APPLICATION_NAME = "veilarbdirigent";

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
    }
}