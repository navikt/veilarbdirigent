package no.nav.veilarbdirigent.repository;

import no.nav.veilarbdirigent.mock.LocalH2Database;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

class FeedRepositoryTest {

    private FeedRepository dao = new FeedRepository(LocalH2Database.getDb());

    @Test
    void should_be_able_to_update_and_return_the_last_saved_id_record(){
        long newId = 10L;
        dao.oppdaterSisteKjenteId(newId);

        assertThat(dao.sisteKjenteId()).isEqualTo(newId);
    }
}