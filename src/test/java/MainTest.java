import no.nav.brukerdialog.security.Constants;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.ServiceUser;
import no.nav.fo.veilarbdirigent.config.DatabaseTestContext;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.APPLICATION_NAME;
import static no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConsumerConfig.VEILARBOPPFOLGINGAPI_URL_PROPERTY;

public class MainTest {

    private static final String TEST_PORT = "8890";

    public static void main(String[] args) throws Exception {
        setProperty("SERVICE_CALLS_HOME", "target/log");
        setProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY, "http://localhost:8080/veilarboppfolging/api");
        
        ApiAppTest.setupTestContext();
        DatabaseTestContext.setupContext(getProperty("database"));

        setupSecurity();
        Main.main(TEST_PORT);
    }

    public static void setupSecurity(){
        String issoHost = FasitUtils.getBaseUrl("isso-host");
        String issoJWS = FasitUtils.getBaseUrl("isso-jwks");
        String issoISSUER = FasitUtils.getBaseUrl("isso-issuer");
        String issoIsAlive = FasitUtils.getBaseUrl("isso.isalive", FasitUtils.Zone.FSS);
        String loginUrl = FasitUtils.getBaseUrl("veilarblogin.redirect-url", FasitUtils.Zone.FSS);
        ServiceUser srvveilarbdirigent = FasitUtils.getServiceUser("srvveilarbdirigent", APPLICATION_NAME);
        ServiceUser isso_rp_user = FasitUtils.getServiceUser("isso-rp-user", APPLICATION_NAME);

        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, issoHost);
        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, isso_rp_user.getUsername());
        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, isso_rp_user.getPassword());
        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, issoJWS);
        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, issoISSUER);
        setProperty(Constants.ISSO_ISALIVE_URL_PROPERTY_NAME, issoIsAlive);
        setProperty(Main.REDIRECT_URL_PROPERTY, loginUrl);
        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, srvveilarbdirigent.getUsername());
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, srvveilarbdirigent.getPassword());
    }

}
