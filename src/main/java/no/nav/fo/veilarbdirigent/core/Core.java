package no.nav.fo.veilarbdirigent.core;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.fo.veilarbdirigent.coreapi.*;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static no.nav.fo.veilarbdirigent.core.Utils.runWithLock;

@Slf4j
public class Core {
    private final List<MessageHandler> handlers;
    private final TaskDAO taskDAO;
    private final LockingTaskExecutor lock;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, Actuator> actuators;

    public Core(
            List<MessageHandler> handlers,
            List<Actuator> actuators,
            LockingTaskExecutor lock,
            ThreadPoolTaskScheduler taskScheduler,
            TaskDAO taskDAO
    ) {

        List<Tuple2<String, Actuator>> actuatorList = List
                .ofAll(actuators)
                .map((actuator) -> Tuple.of(actuator.getType(), actuator));

        this.handlers = handlers;
        this.taskDAO = taskDAO;
        this.lock = lock;
        this.taskScheduler = taskScheduler;
        this.actuators = HashMap.ofEntries(actuatorList);
    }

    public void submit(Message message) {
        List<Task> tasks = handlers.flatMap((handler) -> handler.handle(message));

        taskDAO.insert(tasks);

        taskScheduler.execute(this::runActuators);
    }

    @Scheduled(fixedRate = 10000)
    public void runActuators() {
        runWithLock(lock, "runActuators", () -> taskDAO.fetchTasks().forEach(this::submit));
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
        Option<Actuator> maybeActuator = this.actuators.get(task.getType());
        Actuator<?> actuator = maybeActuator.getOrElseThrow(RuntimeException::new);
        actuator.handle(task);
    }
}
