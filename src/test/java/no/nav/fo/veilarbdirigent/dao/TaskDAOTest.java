package no.nav.fo.veilarbdirigent.dao;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Status;
import no.nav.fo.veilarbdirigent.core.Task;
import no.nav.fo.veilarbdirigent.db.IntegrasjonsTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

class TaskDAOTest extends IntegrasjonsTest {

    TaskDAO dao = getBean(TaskDAO.class);

    @Test
    void persisting_task() {
        List<Task> tasks = List.of(
                Task
                        .builder()
                        .id("id1")
                        .type("TestType")
                        .status(Status.PENDING)
                        .data("Noe data her")
                        .build()
        );

        dao.insert(tasks);

        List<Task> tasksFromDb = dao.fetchTasks();
        assertThat(tasksFromDb.length()).isEqualTo(1);
    }
}