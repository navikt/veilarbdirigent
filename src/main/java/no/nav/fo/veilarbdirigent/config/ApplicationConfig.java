package no.nav.fo.veilarbdirigent.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.fo.veilarbdirigent.admin.AdminController;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.input.InputConfig;
import no.nav.fo.veilarbdirigent.output.OutputConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletContext;

@Configuration
@Import({
        AktorConfig.class,
        CoreConfig.class,
        DbConfig.class,
        DAOConfig.class,
        HandlerConfig.class,
        ClientConfig.class,
        InputConfig.class,
        OutputConfig.class
})
public class ApplicationConfig implements ApiApplication {
    public static final String APPLICATION_NAME = "veilarbdirigent";
    public static final String AKTOER_V2_ENDPOINTURL = "AKTOER_V2_ENDPOINTURL";
    public static final String SECURITYTOKENSERVICE_URL = "SECURITYTOKENSERVICE_URL";


    @Override
    public void startup(ServletContext servletContext) {
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator.sts().issoLogin();
    }

    @Bean
    public AdminController adminController(Core core, TaskDAO dao) {
        return new AdminController(core, dao);
    }
}
