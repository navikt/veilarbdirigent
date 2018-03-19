package no.nav.fo.veilarbdirigent.input.feed;

import no.nav.fo.veilarbdirigent.config.IntegrasjonsTest;
import no.nav.fo.veilarbdirigent.config.databasecleanup.FeedMetadataCleanup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;


@DisplayName("FeedDAO")
class FeedDAOTest extends IntegrasjonsTest implements FeedMetadataCleanup{
    private FeedDAO dao = getBean(FeedDAO.class);

    @Test
    void should_be_able_to_update_and_return_the_last_saved_id_record(){
        long newId = 10L;
        dao.oppdaterSisteKjenteId(newId);

        assertThat(dao.sisteKjenteId()).isEqualTo(newId);
    }

}