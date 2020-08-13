package no.nav.veilarbdirigent.core;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.veilarbdirigent.config.Transactor;
import no.nav.veilarbdirigent.core.api.*;
import no.nav.veilarbdirigent.core.dao.TaskDAO;
import no.nav.veilarbdirigent.utils.TypedField;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.veilarbdirigent.core.Utils.runInMappedDiagnosticContext;
import static no.nav.veilarbdirigent.utils.MetricsUtils.metricName;

@Slf4j
public class Core {
    private static final int LIMIT = 100;
    private List<MessageHandler> handlers = List.empty();
    private Map<TaskType, Actuator> actuators = HashMap.empty();

    private final TaskDAO taskDAO;
    private final ScheduledExecutorService scheduler;
    private final LeaderElectionClient leaderClient;
    private final Transactor transactor;
    private final MetricsClient metricsClient;

    public Core(
            TaskDAO taskDAO,
            ScheduledExecutorService scheduler,
            LeaderElectionClient leaderElectionClient,
            Transactor transactor,
            MetricsClient metricsClient) {
        this.taskDAO = taskDAO;
        this.scheduler = scheduler;
        this.leaderClient = leaderElectionClient;
        this.transactor = transactor;
        this.metricsClient = metricsClient;

        scheduler.scheduleWithFixedDelay(this::runActuators, 0, 10, SECONDS);
    }

    public void registerHandler(MessageHandler handler) {
        this.handlers = this.handlers.append(handler);
    }

    public void registerActuator(TaskType type, Actuator actuator) {
        this.actuators = this.actuators.put(type, actuator);
    }

    @Transactional
    public boolean submit(Message message) {
        Event event = new Event(metricName("submit"));
        try {
            List<Task> tasks = handlers
                    .flatMap((handler) -> handler.handle(message))
                    .map((task) -> task.withStatus(Status.PENDING));


            log.info("Message translated to {} tasks", tasks.length());
            taskDAO.insert(tasks);
            event.addFieldToReport("taskCount", tasks.size());
            event.setSuccess();

            return true;
        } catch (Exception e) {
            log.error("Error while handling message", e);
            event.setFailed();
            throw new RuntimeException(e);
        } finally {
            metricsClient.report(event);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void submitInTransaction(Message message) {
        submit(message);
    }

    public void forceScheduled() {
        scheduler.execute(this::runActuatorsInMDC);
    }

    private void runActuatorsInMDC() {
        runInMappedDiagnosticContext("runActuators", UUID.randomUUID().toString(), this::runActuators);
    }

    private void runActuators() {
        if (leaderClient.isLeader()) {
            try {
                List<Task> tasks = taskDAO.fetchTasksReadyForExecution(LIMIT);
                log.info("Actuators scheduled: {} Task ready to be executed", tasks.length());

                var event = new Event(metricName("runActuators"));
                event.addFieldToReport("count", tasks.size());
                metricsClient.report(event);

                tasks.forEach(this::tryActuatorsInMDC);
                if (tasks.length() >= LIMIT) {
                    log.info("Tasks was equal to limit. Start next schedule at once");
                    forceScheduled();
                }
            } catch (Exception e) {
                log.error("runActuators crashed", e);
            }
        } else {
            //This should not happen. Since there should only be one pod
            log.warn("Not leader, Do not run actuators");
        }
    }

    private void tryActuatorsInMDC(Task<?, ?> task) {
        runInMappedDiagnosticContext("taskId", task.getId(), () -> tryActuators(task));
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
            Event event = new Event(metricName("tryActuator")).addFieldToReport("type", task.getType());
            Try.of(() -> actuator.handle(task.getData().element))
                    .flatMap(Function.identity())
                    .onFailure(throwable -> {
                        log.error(throwable.getMessage(), throwable);
                        Task taskWithError = task.withError(throwable.toString());
                        taskDAO.setStatusForTask(taskWithError, Status.FAILED);
                        event.setFailed();
                        metricsClient.report(event);
                    })
                    .onSuccess(result -> {
                        log.info("Task:{} completed successfully", task.getId());
                        Task taskWithResult = task.withResult(new TypedField<>(result));
                        taskDAO.setStatusForTask(taskWithResult, Status.OK);
                        event.setSuccess();
                        metricsClient.report(event);
                    });
        });
    }

}
