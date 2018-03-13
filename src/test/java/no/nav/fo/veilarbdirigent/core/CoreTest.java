package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoreTest {
    private TaskDAO dao = mock(TaskDAO.class);
    private Actuator actuator = mock(Actuator.class);
    private MessageHandler handler = mock(MessageHandler.class);

    @BeforeEach
    public void setup() {
        List<Task> tasks = List.of(
                TestUtils.createTask("id1", "type", "data"),
                TestUtils.createTask("id2", "type", "data")
        );

        when(handler.handle(any())).thenReturn(tasks);
        when(dao.fetchTasks()).thenReturn(tasks);
    }

    @AfterEach
    public void teardown() {
        reset(handler);
        reset(dao);
    }

    @Test
    void name() {
        CoreOut coreOut = new CoreOut(List.of(actuator), dao);
        CoreIn coreIn = new CoreIn(coreOut, dao, List.of(handler));

        Message message = new Message() {
        };
        coreIn.submit(message);

        ArgumentCaptor<List<Task>> captor = TestUtils.listArgumentCaptor(Task.class);

        verify(handler, times(1)).handle(any(Message.class));
        verify(dao, times(1)).insert(captor.capture());

        assertThat(captor.getValue().length()).isEqualTo(2);
        verify(dao, times(1)).fetchTasks();

        verify(actuator, times(2)).handle(any(Task.class));
    }
}