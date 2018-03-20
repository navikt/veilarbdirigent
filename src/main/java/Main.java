
import no.nav.apiapp.ApiApp;
import no.nav.brukerdialog.security.Constants;
import no.nav.fo.veilarbdirigent.config.ApplicationConfig;
import no.nav.fo.veilarbdirigent.config.DbConfig;
import no.nav.fo.veilarbdirigent.config.MigrationUtils;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class Main {

    public static final String REDIRECT_URL_PROPERTY = "VEILARBLOGIN_REDIRECT_URL_URL";

    public static void main(String... args) {
        MigrationUtils.createTables(DbConfig.getDataSource());
        System.setProperty(Constants.OIDC_REDIRECT_URL_PROPERTY_NAME, getRequiredProperty(REDIRECT_URL_PROPERTY));

        ApiApp.startApp(ApplicationConfig.class, args);
    }

}
