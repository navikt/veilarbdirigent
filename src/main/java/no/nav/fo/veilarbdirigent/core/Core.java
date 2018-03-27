package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
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
        runWithLock(lock, "runActuators", () -> taskDAO.fetchTasksReadyForExecution().forEach(this::tryActuators));
    }

    @SuppressWarnings("unchecked")
    private void tryActuators(Task<?, ?> task) {
        this.actuators.get(task.getType()).forEach((actuator) -> tryActuator(actuator, task));
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
                        Task taskWithResult = task.withResult(new TypedField<>(result));
                        taskDAO.setStatusForTask(taskWithResult, Status.OK);
                    });
        });
    }

}
