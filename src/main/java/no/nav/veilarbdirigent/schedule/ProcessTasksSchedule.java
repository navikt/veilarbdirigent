package no.nav.veilarbdirigent.schedule;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.JobRunner;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.json.JsonUtils;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import no.nav.veilarbdirigent.service.TaskProcessorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static no.nav.veilarbdirigent.utils.MetricsUtils.metricName;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessTasksSchedule {

    private final static int LIMIT = 100;

    private final static long TEN_SECONDS = 10 * 1000;

    private final TaskRepository taskRepository;

    private final LeaderElectionClient leaderElectionClient;

    private final MetricsClient metricsClient;

    private final TaskProcessorService taskProcessorService;

    @Scheduled(initialDelay = TEN_SECONDS, fixedRate = TEN_SECONDS)
    public void schedule() {
        if (leaderElectionClient.isLeader()) {
            JobRunner.run("process_tasks", this::processTasks);
        }
    }

    private void processTasks() {
        List<Task> tasks = taskRepository.fetchTasksReadyForExecution(LIMIT);
        log.info("Actuators scheduled: {} Task ready to be executed", tasks.size());

        Event event = new Event(metricName("runActuators"));
        event.addFieldToReport("count", tasks.size());
        metricsClient.report(event);

        tasks.forEach(this::performTask);
    }

    private void performTask(Task task) {
        taskRepository.setStatusForTask(task, TaskStatus.WORKING);

        Event event = new Event(metricName("tryActuator")).addFieldToReport("type", task.getType());

        Try.of(() -> taskProcessorService.processTask(task))
                .flatMap(Function.identity())
                .onFailure(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    Task taskWithError = task.withError(throwable.toString());
                    taskRepository.setStatusForTask(taskWithError, TaskStatus.FAILED);
                    event.setFailed();
                    metricsClient.report(event);
                })
                .onSuccess(result -> {
                    log.info("Task:{} completed successfully", task.getId());
                    Task taskWithResult = task.withJsonResult(JsonUtils.toJson(result));
                    taskRepository.setStatusForTask(taskWithResult, TaskStatus.OK);
                    event.setSuccess();
                    metricsClient.report(event);
                });
    }

}
