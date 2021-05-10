package no.nav.veilarbdirigent.repository;

import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.mock.LocalH2Database;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static java.time.LocalDateTime.now;
import static no.nav.veilarbdirigent.repository.TaskRepository.TASK_TABLE;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskRepositoryTest {

    private JdbcTemplate db = LocalH2Database.getDb();

    private TaskRepository dao = new TaskRepository(db);

    @BeforeEach
    void cleanup() {
        db.update("DELETE FROM " + TASK_TABLE);
    }

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

    @Test
    void hasTask_skal_sjekke_om_task_finnes() {
        String data = "Noe data her";
        Task task1 = TestUtils.createTask("id1", data);
        Task task2 = TestUtils.createTask("id2", data);

        List<Task> tasks = List.of(task1, task2);

        dao.insert(tasks);

        assertTrue(dao.hasTask("id2"));
        assertFalse(dao.hasTask("id3"));
    }

}