package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import net.javacrumbs.shedlock.core.SchedulerLock;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import no.nav.sbl.jdbc.Transactor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;

public class CoreOut {
    private final List<Actuator> actuators;
    private final Transactor transactor;
    private final TaskDAO taskDAO;

    @Inject
    public CoreOut(List<Actuator> actuators, Transactor transactor, TaskDAO taskDAO) {
        this.actuators = actuators;
        this.transactor = transactor;
        this.taskDAO = taskDAO;
    }

    @Async
    public void submit(Task task) {
        // TODO start scheduled task for actuators
        transactor.inTransaction(() -> {
            try {
                actuators.forEach((actuator) -> actuator.handle(task));
                taskDAO.setStateForTask(task, Status.OK);
            } catch (Exception e) {
                taskDAO.setStateForTask(task, Status.FAILED);
            }
        });
    }

    @Async
    @Scheduled(fixedRate = 10000)
    @SchedulerLock(name = "fetchFailedTasks")
    public void runActuators() {
        taskDAO.fetchTasks().forEach(this::submit);
    }
}
