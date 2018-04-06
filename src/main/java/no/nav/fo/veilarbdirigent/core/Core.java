package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.fo.veilarbdirigent.core.api.*;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.utils.TypedField;
import no.nav.sbl.jdbc.Transactor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import static no.nav.fo.veilarbdirigent.core.Utils.runWithLock;

@Slf4j
public class Core {
    private List<MessageHandler> handlers = List.empty();
    private Map<TaskType, Actuator> actuators = HashMap.empty();

    private final TaskDAO taskDAO;
    private final ScheduledExecutorService scheduler;
    private final LockingTaskExecutor lock;
    private final Transactor transactor;

    public Core(
            TaskDAO taskDAO,
            ScheduledExecutorService scheduler,
            LockingTaskExecutor lock,
            Transactor transactor
    ) {
        this.taskDAO = taskDAO;
        this.scheduler = scheduler;
        this.lock = lock;
        this.transactor = transactor;
    }

    public void registerHandler(MessageHandler handler) {
        this.handlers = this.handlers.append(handler);
    }

    public void registerActuator(TaskType type, Actuator actuator) {
        this.actuators = this.actuators.put(type, actuator);
    }

    @Transactional
    public boolean submit(Message message) {
        try {
            List<Task> tasks = handlers
                    .flatMap((handler) -> handler.handle(message))
                    .map((task) -> task.withStatus(Status.PENDING));

            log.info("Message translated to {} tasks", tasks.length());
            taskDAO.insert(tasks);

            scheduler.execute(this::runActuators);
            return true;
        } catch (Exception e) {
            log.error("Error while handling message", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean submitInTransaction(Message message) {
        return submit(message);
    }

    @Scheduled(fixedDelay = 10000)
    public void runActuators() {
        runWithLock(lock, "runActuators", () -> {
            List<Task> tasks = taskDAO.fetchTasksReadyForExecution();
            log.info("Actuators scheduled: {} Task ready to be executed", tasks.length());
            tasks.forEach(this::tryActuators);
        });
    }

    @SuppressWarnings("unchecked")
    private void tryActuators(Task<?, ?> task) {
        Option<Actuator> maybeActuator = this.actuators.get(task.getType());
        maybeActuator.forEach((actuator) -> {
            log.info("Trying to run Actuator:{} on Task:{}", task.getType().getType(), task.getId());
            tryActuator(actuator, task);
        });
    }

    private <DATA, RESULT> void tryActuator(Actuator<DATA, RESULT> actuator, Task<DATA, RESULT> task) {
        transactor.inTransaction(() -> {
            taskDAO.setStatusForTask(task, Status.WORKING);
            Try.of(() -> actuator.handle(task.getData().element))
                    .flatMap(Function.identity())
                    .onFailure(throwable -> {
                        log.error(throwable.getMessage(), throwable);
                        Task taskWithError = task.withError(throwable.toString());
                        taskDAO.setStatusForTask(taskWithError, Status.FAILED);
                    })
                    .onSuccess(result -> {
                        log.info("Task:{} completed successfully", task.getId());
                        Task taskWithResult = task.withResult(new TypedField<>(result));
                        taskDAO.setStatusForTask(taskWithResult, Status.OK);
                    });
        });
    }

}
