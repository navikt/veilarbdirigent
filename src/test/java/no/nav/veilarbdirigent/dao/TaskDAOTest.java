package no.nav.veilarbdirigent.dao;

import io.vavr.collection.List;
import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.core.api.Status;
import no.nav.veilarbdirigent.core.api.Task;
import no.nav.veilarbdirigent.core.dao.TaskDAO;
import no.nav.veilarbdirigent.mock.LocalH2Database;
import org.junit.jupiter.api.Test;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Java6Assertions.assertThat;

class TaskDAOTest {

    private TaskDAO dao = new TaskDAO(LocalH2Database.getDb());

    @Test
    void persisting_task() {
        String data = "Noe data her";
        List<Task> tasks = List.of(
                TestUtils.createTask("id1", data)
        );

        dao.insert(tasks);

        List<Task> tasksFromDb = dao.fetchTasksReadyForExecution(100);
        assertThat(tasksFromDb.length()).isEqualTo(1);

        Task task = tasksFromDb.get(0);
        assertThat(task.getId()).isEqualTo("id1");
        assertThat(task.getStatus()).isEqualTo(Status.PENDING);
        assertThat(task.getData().element).isEqualTo(data);
        assertThat(task.getCreated()).isBetween(now().minusSeconds(1), now().plusSeconds(1));
        assertThat(task.getNextAttempt()).isBetween(now().minusSeconds(1), now().plusSeconds(1));
    }
}