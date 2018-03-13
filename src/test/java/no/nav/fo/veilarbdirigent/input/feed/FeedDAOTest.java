package no.nav.fo.veilarbdirigent.input.feed;

import no.nav.fo.veilarbdirigent.db.IntegrasjonsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@DisplayName("FeeDAO")
class FeedDAOTest extends IntegrasjonsTest {
    FeedDAO dao = getBean(FeedDAO.class);

    @Test
    void should_be_able_to_update_and_return_the_last_saved_id_record(){
        long newId = 10L;
        dao.oppdaterSisteKjenteId(newId);

        assertThat(dao.sisteKjenteId(), equalTo(newId));
    }

}