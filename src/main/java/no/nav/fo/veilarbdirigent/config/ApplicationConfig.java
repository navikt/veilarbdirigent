package no.nav.fo.veilarbdirigent.config;

import io.vavr.collection.List;
import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.fo.veilarbdirigent.core.Actuator;
import no.nav.fo.veilarbdirigent.core.CoreIn;
import no.nav.fo.veilarbdirigent.core.CoreOut;
import no.nav.fo.veilarbdirigent.core.MessageHandler;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import static no.nav.apiapp.ApiApplication.Sone.FSS;

@Configuration
@Import({DbConfig.class,
        DAOConfig.class,
        MessageHandlerConfig.class,
        ActuatorConfig.class,
        OppfolgingFeedConfig.class})
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

    @Bean
    public CoreOut coreOut(java.util.List<Actuator> actuators, TaskDAO taskDAO) {
        return new CoreOut(List.ofAll(actuators), taskDAO);
    }

    @Bean
    public CoreIn coreIn(CoreOut coreOut, TaskDAO taskDAO, java.util.List<MessageHandler> handlers) {
        return new CoreIn(coreOut, taskDAO, List.ofAll(handlers));
    }
}