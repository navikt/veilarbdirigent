package no.nav.fo.veilarbdirigent.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.sbl.rest.RestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;

import static no.nav.apiapp.ApiApplication.Sone.FSS;

@Configuration
@Import({
        CoreConfig.class,
        DbConfig.class,
        DAOConfig.class,
        HandlerConfig.class,
        FeedConfig.class,
        ServiceConfig.class
})
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
    public void startup(ServletContext servletContext) {
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator.issoLogin().sts();

    }

    @Bean
    public Client client() {
        return RestUtils.createClient();
    }

}