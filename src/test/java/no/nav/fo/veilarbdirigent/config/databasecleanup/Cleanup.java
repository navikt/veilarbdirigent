package no.nav.fo.veilarbdirigent.config.databasecleanup;

import org.springframework.jdbc.core.JdbcTemplate;

public interface Cleanup {
    JdbcTemplate getJdbc();
}
