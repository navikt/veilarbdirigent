package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.config.CoreConfig;
import no.nav.fo.veilarbdirigent.config.DAOConfig;
import no.nav.fo.veilarbdirigent.config.DbConfig;
import no.nav.fo.veilarbdirigent.coreapi.Actuator;
import no.nav.fo.veilarbdirigent.coreapi.MessageHandler;
import no.nav.fo.veilarbdirigent.coreapi.Status;
import no.nav.fo.veilarbdirigent.coreapi.Task;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.db.AbstractIntegrationTest;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import static no.nav.fo.veilarbdirigent.TestUtils.delay;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreIntegrationTest extends AbstractIntegrationTest {

    private Core core = getBean(Core.class);
    private TaskDAO dao = getBean(TaskDAO.class);
    private MessageHandler messageHandler = getBean(MessageHandler.class);
    private Actuator actuator = getBean(Actuator.class);

    private static final List<Task> TASKS = List.of(
            TestUtils.createTask("id0", "data"),
            TestUtils.createTask("id1", "data"),
            TestUtils.createTask("id2", "data"),
            TestUtils.createTask("id3", "data")
    );

    @BeforeAll
    @BeforeClass
    static void setupContext() {
        setupContext(
                CoreConfig.class,
                DbConfig.class,
                DAOConfig.class,
                CoreIntegrationTest.TestConfig.class
        );
    }

    @BeforeEach
    void setup() {
        when(messageHandler.handle(any())).thenReturn(TASKS);
    }

    @Test
    void core_should_transform_messages_to_tasks_save_them_and_execute_them() {
        throwExceptionOnTaskWithId(TASKS.get(2).getId());
        core.submit(null);

        List<Task> savedTasks = dao.fetchTasks();
        assertThat(savedTasks.length()).isEqualTo(4);

        delay(200);

        List<Task> nonCompletedTasks = dao.fetchTasks();
        assertThat(nonCompletedTasks.length()).isEqualTo(1);
        assertThat(nonCompletedTasks.get(0).getId()).isEqualToIgnoringCase("id2");
        assertThat(nonCompletedTasks.get(0).getStatus()).isEqualByComparingTo(Status.FAILED);
    }

    @SuppressWarnings("unchecked")
    private void throwExceptionOnTaskWithId(String taskId) {
        when(actuator.handle(any())).thenAnswer(a -> {
            Task task = (Task) a.getArguments()[0];
            if (taskId.equals(task.getId())) {
                throw new RuntimeException();
            }
            return task;
        });
    }

    static class TestConfig {
        @Bean
        public Actuator actuator() {
            Actuator mock = mock(Actuator.class);
            when(mock.getType()).thenReturn(TestUtils.TASK_TYPE);
            return mock;
        }

        @Bean
        public MessageHandler messageHandler() {
            return mock(MessageHandler.class);
        }
    }
}