import no.nav.apiapp.ApiApp;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.common.utils.NaisUtils;
import no.nav.fo.veilarbdirigent.config.ApplicationConfig;
import no.nav.fo.veilarbdirigent.config.DbConfig;
import no.nav.fo.veilarbdirigent.config.MigrationUtils;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;

import static no.nav.brukerdialog.security.Constants.OIDC_REDIRECT_URL_PROPERTY_NAME;
import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.fo.veilarbdirigent.config.DbConfig.VEILARBDIRIGENTDB_PASSWORD;
import static no.nav.fo.veilarbdirigent.config.DbConfig.VEILARBDIRIGENTDB_USERNAME;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class Main {

    public static final String REDIRECT_URL_PROPERTY = "VEILARBLOGIN_REDIRECT_URL_URL";

    public static void main(String... args) {
        readFromConfigMap();

        NaisUtils.Credentials serviceUser = getCredentials("service_user");

        //CXF
        System.setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, serviceUser.password);

        //OIDC
        System.setProperty(SecurityConstants.SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, serviceUser.password);

        System.setProperty(OIDC_REDIRECT_URL_PROPERTY_NAME, getRequiredProperty(REDIRECT_URL_PROPERTY));

        NaisUtils.Credentials oracleCreds = getCredentials("oracle_creds");
        System.setProperty(VEILARBDIRIGENTDB_USERNAME, oracleCreds.username);
        System.setProperty(VEILARBDIRIGENTDB_PASSWORD, oracleCreds.password);

        MigrationUtils.createTables(DbConfig.getDataSource());
        ApiApp.runApp(ApplicationConfig.class, args);
    }


    private static void readFromConfigMap() {
        NaisUtils.addConfigMapToEnv("pto-config",
                "SECURITYTOKENSERVICE_URL",
                "ISSO_HOST_URL",
                "ISSO_JWKS_URL",
                "ISSO_ISSUER_URL",
                "ISSO_ISALIVE_URL",
                "VEILARBLOGIN_REDIRECT_URL_DESCRIPTION",
                "VEILARBLOGIN_REDIRECT_URL_URL",
                "LOGINSERVICE_OIDC_CALLBACKURI",
                "LOGINSERVICE_OIDC_DISCOVERYURI",
                "SECURITY_TOKEN_SERVICE_OPENID_CONFIGURATION_URL"
        );
    }

}
