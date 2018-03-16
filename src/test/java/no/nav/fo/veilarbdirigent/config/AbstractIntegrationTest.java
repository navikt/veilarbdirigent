package no.nav.fo.veilarbdirigent.config;

import lombok.SneakyThrows;
import no.nav.fo.veilarbdirigent.db.DatabaseTestContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static java.lang.System.setProperty;

public class AbstractIntegrationTest {
    private static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    private static PlatformTransactionManager platformTransactionManager;
    private TransactionStatus transactionStatus;

    @SneakyThrows
    protected static void setupContext(Class<?>... classes) {
        DatabaseTestContext.setupInMemoryContext();
        MigrationUtils.createTables(DbConfig.getDataSource());

        setProperty("no.nav.modig.security.systemuser.username", "username");
        setProperty("no.nav.modig.security.systemuser.password", "password");

        annotationConfigApplicationContext = new AnnotationConfigApplicationContext(classes);
        annotationConfigApplicationContext.start();
        platformTransactionManager = getBean(PlatformTransactionManager.class);

    }

    protected static <T> T getBean(Class<T> requiredType) {
        return annotationConfigApplicationContext.getBean(requiredType);
    }

    @BeforeEach
    @Before
    public void injectAvhengigheter() {
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @BeforeEach
    @Before
    public void startTransaksjon() {
        transactionStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
    }

    @AfterEach
    @After
    public void rollbackTransaksjon() {
        if (platformTransactionManager != null && transactionStatus != null) {
            platformTransactionManager.rollback(transactionStatus);
        }
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
}
