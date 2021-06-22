package no.nav.veilarbdirigent.utils;

import no.nav.common.health.HealthCheckResult;
import org.springframework.jdbc.core.JdbcTemplate;

public class DbUtils {

    public static HealthCheckResult checkDbHealth(JdbcTemplate db) {
        try {
            db.query("SELECT 1 FROM DUAL", resultSet -> {});
            return HealthCheckResult.healthy();
        } catch (Exception e) {
            return HealthCheckResult.unhealthy("Fikk ikke kontakt med databasen", e);
        }
    }

}
