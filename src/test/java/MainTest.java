import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.ServiceUser;
import no.nav.fo.veilarbdirigent.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.APPLICATION_NAME;

public class MainTest {

    private static final String TEST_PORT = "8800";

    public static void main(String[] args) throws Exception {
        setProperty("SERVICE_CALLS_HOME", "target/log");
        ApiAppTest.setupTestContext();
        DatabaseTestContext.setupContext(getProperty("database"));

        String securityTokenService = FasitUtils.getBaseUrl("securityTokenService");
        ServiceUser srvveilarbdirigent = FasitUtils.getServiceUser("srvveilarbdirigent", APPLICATION_NAME);

        setProperty(StsSecurityConstants.STS_URL_KEY, securityTokenService);

        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, srvveilarbdirigent.getUsername());
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, srvveilarbdirigent.getPassword());

        Main.main(TEST_PORT);
    }

}
