package no.nav.veilarbdirigent.repository;

import io.vavr.Tuple;
import io.vavr.collection.Map;
import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.mock.LocalH2Database;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.veilarbdirigent.repository.TaskRepository.TASK_TABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskRepositoryTest {

    private NamedParameterJdbcTemplate db = new NamedParameterJdbcTemplate(LocalH2Database.getDb());

    private TaskRepository dao = new TaskRepository(db);

    @BeforeEach
    void cleanup() {
        db.getJdbcTemplate().execute("DELETE FROM " + TASK_TABLE);
    }

    @Test
    void persisting_task() {
        String data = "Noe data her";
        dao.insert(TestUtils.createTask("id1", data));

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

        dao.insert(task1);
        dao.insert(task2);

        assertTrue(dao.hasTask("id2"));
        assertFalse(dao.hasTask("id3"));
    }

    @Test
    void runNow_skal_klargjore_feilet_task_for_kjøring() {
        String data = "{runMe: true}";
        Task task1 = TestUtils.createTask("id1", data);
        Task task2 = TestUtils.createTask("id2", data);

        dao.insert(task1);
        dao.insert(task2);

        Task failingTask = task1.toBuilder().error("Something fishy happened").build();
        dao.setStatusForTask(failingTask, TaskStatus.FAILED);

        List<Task> failedTasks = dao.fetchAllFailedTasks();
        assertThat(failedTasks).hasSize(1);
        Task failedTask = failedTasks.get(0);
        assertThat(failedTask.getError()).isEqualTo("Something fishy happened");
        assertThat(failedTask.getAttempts()).isEqualTo(1);
        assertThat(failedTask.getNextAttempt()).isAfter(LocalDateTime.now());


        List<Task> readyForExecutionBefore = dao.fetchTasksReadyForExecution(100);
        assertThat(readyForExecutionBefore).hasSize(1);
        dao.runNow("id1");

        List<Task> readyForExecution = dao.fetchTasksReadyForExecution(100);

        assertThat(readyForExecution).hasSize(2);

    }

    @Test
    void status_ok_skal_inneholde_result() {
        String data = "{runMe: true}";
        Task task1 = TestUtils.createTask("id1", data);
        Task task2 = TestUtils.createTask("id2", data);

        dao.insert(task1);
        dao.insert(task2);

        Task okTask = task1.toBuilder().jsonResult("{result: OK}").build();

        dao.setStatusForTask(okTask, TaskStatus.OK);

        Optional<Task> optionalTask = dao.fetch("id1");
        assertThat(optionalTask).hasValueSatisfying( t -> assertThat(t.getJsonResult()).isEqualTo("{result: OK}"));


    }

    @Test
    void statusNumbers_skal_telle_statuser() {
        String jsonData = "{runMe: true}";
        var task1 = Task.builder()
                .id("id1")
                .type(TestUtils.TASK_TYPE)
                .jsonData(jsonData)
                .taskStatus(TaskStatus.PENDING)
                .build();
        var task2 = Task.builder()
                .id("id2")
                .type(TestUtils.TASK_TYPE)
                .jsonData(jsonData)
                .taskStatus(TaskStatus.PENDING)
                .build();
        var task3 = Task.builder()
                .id("id3")
                .type(TestUtils.TASK_TYPE)
                .jsonData(jsonData)
                .taskStatus(TaskStatus.PENDING)
                .build();
        dao.insert(task1);
        dao.insert(task2);
        dao.insert(task3);

        var okTask = task1.toBuilder().jsonResult("{result: OK}").build();
        dao.setStatusForTask(okTask, TaskStatus.OK);

        var failingTask = task2.toBuilder().error("Something fishy happened").build();
        dao.setStatusForTask(failingTask, TaskStatus.FAILED);

        Map<String, Integer> statusnumbers = dao.fetchStatusnumbers();

        assertThat(statusnumbers).containsExactlyInAnyOrder(
                Tuple.of(TaskStatus.OK.name(), 1),
                Tuple.of(TaskStatus.FAILED.name(), 1),
                Tuple.of(TaskStatus.PENDING.name(), 1)
        );

    }


}