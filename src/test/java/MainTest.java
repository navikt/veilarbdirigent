import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.fo.veilarbdirigent.config.DatabaseTestContext;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.setProperty;
import static no.nav.fo.veilarbdirigent.TestUtils.setupSecurity;
import static no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConsumerConfig.VEILARBOPPFOLGINGAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbaktivitetService.VEILARBAKTIVITETAPI_URL_PROPERTY;

public class MainTest {

    private static final String TEST_PORT = "8890";

    public static void main(String[] args) throws Exception {
        setProperty("SERVICE_CALLS_HOME", "target/log");
        setProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY, "http://localhost:8080/veilarboppfolging/api");
        setProperty(VEILARBAKTIVITETAPI_URL_PROPERTY, "http://localhost:8080/veilarbaktivitet/api");

        ApiAppTest.setupTestContext();
        DatabaseTestContext.setupContext("T6");

        setupSecurity();
        String loginUrl = FasitUtils.getBaseUrl("veilarblogin.redirect-url", FasitUtils.Zone.FSS);
        setProperty(Main.REDIRECT_URL_PROPERTY, loginUrl);
        Main.main(TEST_PORT);
    }
}
