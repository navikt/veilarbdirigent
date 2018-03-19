package no.nav.fo.veilarbdirigent.config;

import lombok.SneakyThrows;
import no.nav.fo.veilarbdirigent.config.databasecleanup.Cleanup;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static java.lang.System.setProperty;

public abstract class AbstractIntegrationTest implements Cleanup {
    private static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    private JdbcTemplate jdbc = getBean(JdbcTemplate.class);

    @SneakyThrows
    protected static void setupContext(Class<?>... classes) {
        DatabaseTestContext.setupInMemoryContext();
        MigrationUtils.createTables(DbConfig.getDataSource());

        setProperty("no.nav.modig.security.systemuser.username", "username");
        setProperty("no.nav.modig.security.systemuser.password", "password");

        annotationConfigApplicationContext = new AnnotationConfigApplicationContext(classes);
        annotationConfigApplicationContext.start();
    }

    protected static <T> T getBean(Class<T> requiredType) {
        return annotationConfigApplicationContext.getBean(requiredType);
    }

    @BeforeEach
    @Before
    public void injectAvhengigheter() {
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @AfterAll
    @AfterClass
    public static void close() {
        if (annotationConfigApplicationContext != null) {
            annotationConfigApplicationContext.stop();
            annotationConfigApplicationContext.close();
            annotationConfigApplicationContext.destroy();
            annotationConfigApplicationContext = null;
        }
    }

    @Override
    public JdbcTemplate getJdbc() {
        return jdbc;
    }
}
