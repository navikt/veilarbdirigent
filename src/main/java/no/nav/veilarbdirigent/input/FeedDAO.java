package no.nav.veilarbdirigent.input;

import no.nav.sbl.sql.SqlUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public class FeedDAO {
    private static final String TABLE_NAME = "FEED_METADATA";
    private static final String LAST_ID_COLUMN_NAME = "NYE_BRUKERE_FEED_SISTE_ID";
    private final JdbcTemplate jdbc;

    public FeedDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    long sisteKjenteId() {
        return Optional.ofNullable(
                SqlUtils.select(jdbc, TABLE_NAME, (rs) -> rs.getLong(LAST_ID_COLUMN_NAME))
                        .column(LAST_ID_COLUMN_NAME)
                        .execute()
        ).orElse(0L);
    }

    void oppdaterSisteKjenteId(long id){
        SqlUtils.update(jdbc, TABLE_NAME)
                .set(LAST_ID_COLUMN_NAME, id)
                .execute();
    }

}
