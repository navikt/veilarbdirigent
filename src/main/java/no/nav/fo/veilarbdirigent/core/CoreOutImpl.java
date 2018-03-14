package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import static no.nav.fo.veilarbdirigent.core.Utils.runWithLock;

@Slf4j
public class CoreOutImpl implements CoreOut {
    private Map<String, Actuator<?>> actuators = HashMap.empty();
    private final TaskDAO taskDAO;
    private final LockingTaskExecutor lock;

    public CoreOutImpl(TaskDAO taskDAO, LockingTaskExecutor lock) {
        this.taskDAO = taskDAO;
        this.lock = lock;
    }

    private void submit(Task task) {
        Try.run(() -> this.submitTry(task))
                .andThen(() -> taskDAO.setStateForTask(task, Status.OK))
                .orElseRun((Throwable error) -> {
                    log.info("Something went wrong in submit to CoreOutImpl", error);
                    taskDAO.setStateForTask(task, Status.FAILED);
                });
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void submitTry(Task task) {
        Option<Actuator<?>> maybeActuator = this.actuators.get(task.getType());
        Actuator<?> actuator = maybeActuator.getOrElseThrow(RuntimeException::new);
        actuator.handle(task);
    }

    @Async
    @Scheduled(fixedRate = 10000)
    public void runActuators() {
        runWithLock(lock, "runActuators", () -> taskDAO.fetchTasks().forEach(this::submit));
    }

    public void registerActuator(String name, Actuator<?> taskRunner) {
        this.actuators = this.actuators.put(name, taskRunner);
    }
}
