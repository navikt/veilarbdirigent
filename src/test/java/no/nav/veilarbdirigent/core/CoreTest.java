package no.nav.veilarbdirigent.core;

import io.vavr.collection.List;
import io.vavr.control.Try;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.config.CurrentThreadSceduler;
import no.nav.veilarbdirigent.config.Transactor;
import no.nav.veilarbdirigent.core.api.*;
import no.nav.veilarbdirigent.core.dao.TaskDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoreTest {
    private TaskDAO dao = mock(TaskDAO.class);
    private Actuator actuator = mock(Actuator.class);
    private MessageHandler handler = mock(MessageHandler.class);
    private Transactor transactor = TestUtils.getTransactor();
    private ScheduledExecutorService scheduler = new CurrentThreadSceduler();
    private LeaderElectionClient leaderElectionClient = mock(LeaderElectionClient.class);
    private MetricsClient metricsClient = mock(MetricsClient.class);

    private static final List<Task> TASKS = List.of(
            TestUtils.createTask("id0", "data1"),
            TestUtils.createTask("id1", "data2"),
            TestUtils.createTask("id3", "data3"),
            TestUtils.createTask("id4", "data4")
            );

    private Core core;

    @BeforeEach
    void setup() {
        when(handler.handle(any())).thenReturn(TASKS);
        when(dao.fetchTasksReadyForExecution(100)).thenReturn(TASKS);
        when(leaderElectionClient.isLeader()).thenAnswer(a -> true);

        core = new Core(
                dao,
                scheduler,
                leaderElectionClient,
                transactor,
                metricsClient
        );

        core.registerHandler(handler);
        core.registerActuator(TestUtils.TASK_TYPE, actuator);
    }

    @AfterEach
    void teardown() {
        reset(handler);
        reset(dao);
    }


    @Test
    @SuppressWarnings("unchecked")
    void should_transform_messages_to_tasks_and_try_to_execute_them() {
        when(actuator.handle(any())).thenReturn(Try.success("data"));

        Message message = new Message() {
        };
        core.submit(message);
        core.forceScheduled();

        ArgumentCaptor<List<Task>> captor = TestUtils.listArgumentCaptor(Task.class);

        verify(handler, times(1)).handle(any(Message.class));
        verify(dao, times(1)).insert(captor.capture());

        assertThat(captor.getValue().length()).isEqualTo(4);
        verify(dao, times(1)).fetchTasksReadyForExecution(100);

        verify(actuator, times(4)).handle(any());
        verify(dao, times(4)).setStatusForTask(any(Task.class), eq(Status.OK));
    }


    @Test
    void core_should_handle_when_tasks_fail() {
        mockHandleResult(
                TASKS.get(0).getData().element.toString(),
                TASKS.get(1).getData().element.toString()
        );

        core.submit(null);
        core.forceScheduled();
        verify(dao, times(2)).setStatusForTask(any(Task.class), eq(Status.FAILED));
    }

    @SuppressWarnings("unchecked")
    private void mockHandleResult(String exceptionCase, String failureCase) {
        when(actuator.handle(any())).thenAnswer(a -> {
            String data = (String) a.getArguments()[0];
            if (exceptionCase.equals(data)) {
                throw new RuntimeException();
            }
            else if (failureCase.equals(data)) {
                return Try.failure(new RuntimeException());
            }
            return Try.success(data);
        });
    }



}