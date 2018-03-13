package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class CoreOut {
    private final List<Actuator> actuators;
    private final TaskDAO taskDAO;

    public CoreOut(List<Actuator> actuators, TaskDAO taskDAO) {
        this.actuators = actuators;
        this.taskDAO = taskDAO;
    }

    @Async
    public void submit(Task task) {
        Try.run(() -> actuators.forEach((actuator) -> actuator.handle(task)))
                .andThen(() -> taskDAO.setStateForTask(task, Status.OK))
                .orElseRun((Throwable error) -> {
                    log.info("Something went wrong in submit to CoreOut", error);
                    taskDAO.setStateForTask(task, Status.FAILED);
                });
    }

    @Async
    @Scheduled(fixedRate = 10000)
    @SchedulerLock(name = "fetchFailedTasks")
    public void runActuators() {
        taskDAO.fetchTasks().forEach(this::submit);
    }
}
