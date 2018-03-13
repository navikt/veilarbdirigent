package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.CoreIn;
import no.nav.sbl.sql.SqlUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;

public class OppfolgingFeedService {

    private static final String TABLE_NAME = "FEED_METADATA";
    private static final String LAST_ID_COLUMN_NAME = "LAST_ID_COLUMN_NAME";
    private final DataSource ds;
    private final JdbcTemplate jdbc;
    private final CoreIn core;

    public OppfolgingFeedService(DataSource ds, JdbcTemplate jdbc, CoreIn core) {
        this.ds = ds;
        this.jdbc = jdbc;
        this.core = core;
    }

    long sisteKjenteId() {
        return Optional.ofNullable(
                SqlUtils.select(ds, TABLE_NAME, (rs) -> rs.getLong(0))
                        .column(LAST_ID_COLUMN_NAME)
                        .execute()
        ).orElse(0L);
    }

    private void oppdaterSisteKjenteId(long id){
        SqlUtils.update(jdbc, TABLE_NAME)
                .set(LAST_ID_COLUMN_NAME, id)
                .execute();
    }

    void compute(String lastEntryId, List<OppfolgingDataFraFeed> elements) {
        elements.map(element -> {
                    core.submit(element);
                    return element.getId();
                })
                .lastOption()
                .forEach(this::oppdaterSisteKjenteId);
    }
}
