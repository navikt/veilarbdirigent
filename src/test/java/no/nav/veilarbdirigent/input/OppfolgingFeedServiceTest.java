package no.nav.veilarbdirigent.input;

import io.vavr.collection.List;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.config.Transactor;
import no.nav.veilarbdirigent.core.Core;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OppfolgingFeedService")
class OppfolgingFeedServiceTest {

    private Core core = mock(Core.class);
    private FeedDAO feedDao = mock(FeedDAO.class);
    private Transactor transactor = TestUtils.getTransactor();
    private LeaderElectionClient leaderElectionClient = mock(LeaderElectionClient.class);
    private OppfolgingFeedService oppfolgingFeedService = new OppfolgingFeedService(core, feedDao, transactor, leaderElectionClient);

    @BeforeEach
    void setup() {
        when(leaderElectionClient.isLeader()).thenAnswer(a-> true);
    }

    private OppfolgingDataFraFeed newOppfolgingDataFraFeed(long id) {
        return OppfolgingDataFraFeed
                .builder()
                .id(id)
                .aktorId("123")
                .opprettet(new Date())
                .build();
    }

    private List<OppfolgingDataFraFeed> elements = List.of(
            newOppfolgingDataFraFeed(1),
            newOppfolgingDataFraFeed(2),
            newOppfolgingDataFraFeed(3)
    );

    @Test
    void submits_the_received_message_to_the_core() {
        oppfolgingFeedService.compute("dosn't matter", List.of(elements.head()));
        verify(core).submitInTransaction(elements.head());
    }

    @Test
    void submits_each_message_to_core() {
        oppfolgingFeedService.compute("dosn't matter", elements);
        verify(core, times(elements.length())).submitInTransaction(any());
    }

    @Test
    void tells_feedDab_to_update_last_received_id_to_the_last_element_in_the_feed() {
        oppfolgingFeedService.compute("dosn't matter", elements);
        verify(feedDao).oppdaterSisteKjenteId(elements.last().getId());
    }


    @Test
    void stops_executing_when_a_message_fails() {
        doThrow(new RuntimeException()).when(core).submitInTransaction(elements.get(1));
        assertThrows(RuntimeException.class, () -> oppfolgingFeedService.compute("dosn't matter", elements));

        verify(core, times(2)).submitInTransaction(any());
        verify(feedDao, times(1)).oppdaterSisteKjenteId(anyLong());
    }
}