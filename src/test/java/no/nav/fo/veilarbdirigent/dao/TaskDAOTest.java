package no.nav.fo.veilarbdirigent.dao;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.core.Status;
import no.nav.fo.veilarbdirigent.core.Task;
import no.nav.fo.veilarbdirigent.db.IntegrasjonsTest;
import org.junit.jupiter.api.Test;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Java6Assertions.assertThat;

class TaskDAOTest extends IntegrasjonsTest {

    TaskDAO dao = getBean(TaskDAO.class);

    @Test
    void persisting_task() {
        String data = "Noe data her";
        List<Task> tasks = List.of(
                TestUtils.createTask("id1", data)
        );

        dao.insert(tasks);

        List<Task> tasksFromDb = dao.fetchTasks();
        assertThat(tasksFromDb.length()).isEqualTo(1);

        Task task = tasksFromDb.get(0);
        assertThat(task.getId()).isEqualTo("id1");
        assertThat(task.getStatus()).isEqualTo(Status.PENDING);
        assertThat(task.getData()).isEqualTo(data);
        assertThat(task.getCreated()).isBetween(now().minusSeconds(1), now().plusSeconds(1));
        assertThat(task.getNextAttempt()).isBetween(now().minusSeconds(1), now().plusSeconds(1));
    }
}