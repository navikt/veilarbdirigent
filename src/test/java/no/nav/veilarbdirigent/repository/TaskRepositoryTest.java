package no.nav.veilarbdirigent.repository;

import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.mock.LocalH2Database;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Java6Assertions.assertThat;

class TaskRepositoryTest {

    private TaskRepository dao = new TaskRepository(LocalH2Database.getDb());

    @Test
    void persisting_task() {
        String data = "Noe data her";
        List<Task> tasks = List.of(
                TestUtils.createTask("id1", data)
        );

        dao.insert(tasks);

        List<Task> tasksFromDb = dao.fetchTasksReadyForExecution(100);
        assertThat(tasksFromDb.size()).isEqualTo(1);

        Task task = tasksFromDb.get(0);
        assertThat(task.getId()).isEqualTo("id1");
        assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getJsonData()).isEqualTo(data);
        assertThat(task.getCreated()).isBetween(now().minusSeconds(1), now().plusSeconds(1));
        assertThat(task.getNextAttempt()).isBetween(now().minusSeconds(1), now().plusSeconds(1));
    }
}