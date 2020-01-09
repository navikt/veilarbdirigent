import no.nav.fo.veilarbdirigent.config.DatabaseTestContext;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.fo.veilarbdirigent.TestUtils.setupSecurity;
import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.APPLICATION_NAME;
import static no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConsumerConfig.VEILARBOPPFOLGINGAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.services.MalverkService.VEILARBMALVERKAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.services.VeilarbaktivitetService.VEILARBAKTIVITETAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.services.VeilarbdialogService.VEILARBDIALOGAPI_URL_PROPERTY;
import static no.nav.testconfig.ApiAppTest.Config.builder;

public class MainTest {

    private static final String TEST_PORT = "8890";

    //TODO: Does not work local pc. Fix this
    public static void main(String[] args) throws Exception {
        setProperty("SERVICE_CALLS_HOME", "target/log");
        setProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY, "http://localhost:8080/veilarboppfolging/api");
        setProperty(VEILARBAKTIVITETAPI_URL_PROPERTY, "http://localhost:8080/veilarbaktivitet/api");
        setProperty(VEILARBMALVERKAPI_URL_PROPERTY, "http://localhost:8080/veilarbmalverk/api");
        setProperty(VEILARBDIALOGAPI_URL_PROPERTY, "http://localhost:8080/veilarbdialog/api");

        ApiAppTest.setupTestContext(builder().applicationName(APPLICATION_NAME).build());
        DatabaseTestContext.setupContext(getProperty("database"));

        setupSecurity();
        setProperty(Main.REDIRECT_URL_PROPERTY, "");
        Main.main(TEST_PORT);
    }
}
