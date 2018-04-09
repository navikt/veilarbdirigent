package no.nav.fo.veilarbdirigent.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.fo.veilarbdirigent.admin.AdminController;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.input.InputConfig;
import no.nav.fo.veilarbdirigent.output.OutputConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletContext;

import static no.nav.apiapp.ApiApplication.Sone.FSS;

@Configuration
@Import({
        CoreConfig.class,
        DbConfig.class,
        DAOConfig.class,
        HandlerConfig.class,
        ClientConfig.class,
        InputConfig.class,
        OutputConfig.class
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
        apiAppConfigurator.issoLogin();
    }

    @Bean
    public AdminController adminController(TaskDAO dao){
        return new AdminController(dao);
    }
}