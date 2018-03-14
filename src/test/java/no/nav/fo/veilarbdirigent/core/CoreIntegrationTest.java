package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.config.*;
import no.nav.fo.veilarbdirigent.coreapi.Actuator;
import no.nav.fo.veilarbdirigent.coreapi.MessageHandler;
import no.nav.fo.veilarbdirigent.coreapi.Status;
import no.nav.fo.veilarbdirigent.coreapi.Task;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.db.AbstractIntegrationTest;
import no.nav.fo.veilarbdirigent.db.DatabaseTestContext;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.naming.NamingException;

import static no.nav.fo.veilarbdirigent.TestUtils.delay;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    public static void setupContext() throws NamingException {
        DatabaseTestContext.setupInMemoryContext();
        setupContext(
                CoreConfig.class,
                DbConfig.class,
                DAOConfig.class,
                MessageHandlerTestConfig.class,
                ActuatorTestConfig.class
        );
    }

    @BeforeEach
    public void setup() {
        when(messageHandler.handle(any())).thenReturn(TASKS);
    }

    @Test
    void name() {
        given_actuator_error_on_task(2);
        core.submit(null);

        assertThat(dao.fetchTasks().length()).isEqualTo(4);

        delay(200);

        List<Task> nonCompletedTasks = dao.fetchTasks();
        assertThat(nonCompletedTasks.length()).isEqualTo(1);
        assertThat(nonCompletedTasks.get(0).getId()).isEqualToIgnoringCase("id2");
        assertThat(nonCompletedTasks.get(0).getStatus()).isEqualByComparingTo(Status.FAILED);
    }

    private void given_actuator_error_on_task(int failed) {
        when(actuator.handle(TASKS.get(failed))).thenThrow(new RuntimeException());
    }
}