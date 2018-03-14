package no.nav.fo.veilarbdirigent.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import static no.nav.apiapp.ApiApplication.Sone.FSS;

@Configuration
@Import({
        CoreConfig.class,
        DbConfig.class,
        DAOConfig.class,
        MessageHandlerConfig.class,
        ActuatorConfig.class,
        OppfolgingFeedConfig.class
})
public class ApplicationConfig implements ApiApplication.NaisApiApplication {
    public static final String APPLICATION_NAME = "veilarbdirigent";

    @Inject
    private DataSource dataSource;

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
        MigrationUtils.createTables(dataSource);
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {

    }
}