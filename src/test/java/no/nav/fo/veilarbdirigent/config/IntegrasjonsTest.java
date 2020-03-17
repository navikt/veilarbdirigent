package no.nav.fo.veilarbdirigent.config;

import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedServiceConfig;
import no.nav.fo.veilarbdirigent.output.OutputConfig;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;

import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.AKTOER_V2_ENDPOINTURL;
import static no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConsumerConfig.VEILARBOPPFOLGINGAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.services.MalverkService.VEILARBMALVERKAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.services.VeilarbaktivitetService.VEILARBAKTIVITETAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.services.VeilarbdialogService.VEILARBDIALOGAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.services.VeilarbregisteringService.VEILARBREGISTRERINGAPI_URL_PROPERTY;

public class IntegrasjonsTest extends AbstractIntegrationTest {
    @BeforeAll
    @BeforeClass
    public static void setupContext() {
        DatabaseTestContext.setupInMemoryContext();
        System.setProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY, "http://localhost:1234");
        System.setProperty(VEILARBAKTIVITETAPI_URL_PROPERTY, "http://localhost:12345");
        System.setProperty(VEILARBMALVERKAPI_URL_PROPERTY, "http://localhost:12346");
        System.setProperty(VEILARBDIALOGAPI_URL_PROPERTY, "http://localhost:12347");
        System.setProperty(VEILARBREGISTRERINGAPI_URL_PROPERTY, "http://localhost:12348");
        System.setProperty(AKTOER_V2_ENDPOINTURL, "http://localhost:12349");

        TestUtils.setupSecurity();

        setupContext(
                AktorConfig.class,
                CoreConfig.class,
                DbConfig.class,
                DAOConfig.class,
                HandlerConfig.class,
                ClientTestConfig.class,
                OutputConfig.class,
                OppfolgingFeedServiceConfig.class
        );
    }
}
