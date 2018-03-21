package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedServiceConfig;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;

public class IntegrasjonsTest extends AbstractIntegrationTest {
    @BeforeAll
    @BeforeClass
    public static void setupContext() {
        DatabaseTestContext.setupInMemoryContext();
        setupContext(
                CoreConfig.class,
                DbConfig.class,
                DAOConfig.class,
                HandlerConfig.class,
                ServiceConfig.class,
                OppfolgingFeedServiceConfig.class
        );
    }
}
