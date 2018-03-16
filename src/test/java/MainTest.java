import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.ServiceUser;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.setProperty;
import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.APPLICATION_NAME;

public class MainTest {

    private static final String TEST_PORT = "8800";

    public static void main(String[] args) throws Exception {
        ApiAppTest.setupTestContext();

        ServiceUser srvveilarbdirigent = FasitUtils.getServiceUser("srvveilarbdirigent", APPLICATION_NAME);

        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, srvveilarbdirigent.getUsername());
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, srvveilarbdirigent.getPassword());

        Main.main(TEST_PORT);
    }

}
