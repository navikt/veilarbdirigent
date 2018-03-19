package no.nav.fo.veilarbdirigent.config;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;

public class IntegrasjonsTest extends AbstractIntegrationTest {
    @BeforeAll
    @BeforeClass
    public static void setupContext() {
        DatabaseTestContext.setupInMemoryContext();
        setupContext(ApplicationConfig.class);
    }
}
