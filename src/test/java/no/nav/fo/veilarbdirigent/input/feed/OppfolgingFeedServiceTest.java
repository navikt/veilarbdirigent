package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.CoreIn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OppfolgingFeedService")
class OppfolgingFeedServiceTest {

    private CoreIn core = mock(CoreIn.class);
    private FeedDAO feedDao = mock(FeedDAO.class);
    private OppfolgingFeedService oppfolgingFeedService = new OppfolgingFeedService(core, feedDao);


    @Test
    void submits_the_recived_message_to_the_core() {
        oppfolgingFeedService.compute("dosn't matter", List.of(elements.head()));
        verify(core).submit(elements.head());
    }

    @Test
    void submits_each_message_to_core() {
        oppfolgingFeedService.compute("dosn't matter", elements);
        verify(core, times(elements.length())).submit(any());
    }

    @Test
    void tells_feedDao_to_update_last_recived_id_to_the_last_element_in_the_feed() {
        oppfolgingFeedService.compute("dosn't matter", elements);
        verify(feedDao).oppdaterSisteKjenteId(elements.last().getId());
    }


    @Test
    void stops_executing_when_a_message_fails() {
        doThrow(new RuntimeException()).when(core).submit(elements.get(1));
        assertThrows(RuntimeException.class, () -> oppfolgingFeedService.compute("dosn't matter", elements));

        verify(core, times(2)).submit(any());
        verify(feedDao, never()).oppdaterSisteKjenteId(anyLong());
    }

    private OppfolgingDataFraFeed newOppfolgingDataFraFeed(long id) {
        return OppfolgingDataFraFeed
                .builder()
                .id(id)
                .aktorId("123")
                .opprettet(new Date())
                .selvgaende(true)
                .build();
    }

    private List<OppfolgingDataFraFeed> elements = List.of(
            newOppfolgingDataFraFeed(1),
            newOppfolgingDataFraFeed(2),
            newOppfolgingDataFraFeed(3)
    );


}