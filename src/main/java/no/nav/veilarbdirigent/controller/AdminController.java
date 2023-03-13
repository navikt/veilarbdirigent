package no.nav.veilarbdirigent.controller;

import lombok.Builder;
import lombok.Data;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import no.nav.veilarbdirigent.repository.domain.TaskType;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.ws.rs.QueryParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger LOG = getLogger(AdminController.class);

    private final TaskRepository taskRepository;

    public AdminController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public List<FailedTask> failedTasks() {
        return taskRepository.fetchAllFailedTasks().stream()
                .map(task -> FailedTask.builder()
                        .id(task.getId())
                        .type(task.getType())
                        .taskStatus(task.getTaskStatus())
                        .created(task.getCreated())
                        .attempts(task.getAttempts())
                        .nextAttempt(task.getNextAttempt())
                        .lastAttempt(task.getLastAttempt())
                        .error(task.getError())
                        .build()
                ).collect(Collectors.toList());
    }

    @GetMapping("/status")
    public Map<String, Integer> status() {
        return taskRepository.fetchStatusnumbers().toJavaMap();
    }

    @GetMapping("/forcerun")
    public ResponseEntity<String> forceRun() {
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body("NOT_IMPLEMENTED");
    }

    @GetMapping("/task/rerun")
    public String runtask(@QueryParam("taskid") String taskid) {
        LOG.warn("Rerun taskid: " + taskid);
        taskRepository.runNow(taskid);
        return "OK";
    }

    @Data
    @Builder
    public static class FailedTask {
        String id;
        TaskType type;
        TaskStatus taskStatus;
        LocalDateTime created;
        int attempts;
        LocalDateTime nextAttempt;
        LocalDateTime lastAttempt;
        String error;
    }
}
