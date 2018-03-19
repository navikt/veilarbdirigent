package no.nav.fo.veilarbdirigent.core;

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
import no.nav.fo.veilarbdirigent.utils.TypedField;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;

import static no.nav.fo.veilarbdirigent.core.Utils.runInTransaction;
import static no.nav.fo.veilarbdirigent.core.Utils.runWithLock;

@Slf4j
public class Core {
    private List<MessageHandler> handlers = List.empty();
    private Map<TaskType, Actuator> actuators = HashMap.empty();
    private TransactionTemplate transactionTemplate;

    private final TaskDAO taskDAO;
    private final ThreadPoolTaskScheduler scheduler;
    private final LockingTaskExecutor lock;
    private final PlatformTransactionManager transactionManager;

    @PostConstruct
    public void setup() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Core(
            TaskDAO taskDAO,
            ThreadPoolTaskScheduler scheduler,
            LockingTaskExecutor lock,
            PlatformTransactionManager transactionManager
    ) {
        this.taskDAO = taskDAO;
        this.scheduler = scheduler;
        this.lock = lock;
        this.transactionManager = transactionManager;
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
            List<Task> tasks = handlers.flatMap((handler) -> handler.handle(message));

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
        runWithLock(lock, "runActuators", () -> taskDAO.fetchTasks().forEach(this::tryActuator));
    }

    @SuppressWarnings("unchecked")
    private void tryActuator(Task<?, ?> task) {
        this.actuators.get(task.getType())
                .forEach((actuator) -> runInTransaction(transactionTemplate, () -> {
                    taskDAO.setStatusForTask(task, Status.WORKING);

                    Try.of(() -> actuator.handle(task))
                            .onFailure((exception) -> taskDAO.setStatusForTask(task.withError(exception.toString()), Status.FAILED))
                            .onSuccess((result) -> taskDAO.setStatusForTask(task.withResult(new TypedField(result)), Status.OK));
                }));
    }
}
