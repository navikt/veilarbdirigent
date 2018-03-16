
import no.nav.apiapp.ApiApp;
import no.nav.fo.veilarbdirigent.config.ApplicationConfig;
import no.nav.fo.veilarbdirigent.config.DbConfig;
import no.nav.fo.veilarbdirigent.config.MigrationUtils;

public class Main {

    public static void main(String... args) {
        MigrationUtils.createTables(DbConfig.getDataSource());

        ApiApp.startApp(ApplicationConfig.class, args);
    }

}
