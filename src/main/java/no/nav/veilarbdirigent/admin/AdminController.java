package no.nav.veilarbdirigent.admin;

import lombok.Builder;
import lombok.Data;
import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.core.api.Status;
import no.nav.veilarbdirigent.core.api.TaskType;
import no.nav.veilarbdirigent.core.dao.TaskDAO;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger LOG = getLogger(AdminController.class);

    private final Core core;
    private final TaskDAO dao;

    public AdminController(Core core, TaskDAO dao) {
        this.core = core;
        this.dao = dao;
    }

    @GetMapping
    public List<FailedTask> failedTasks() {
        return dao.fetchAllFailedTasks().map(task -> FailedTask.builder()
                .id(task.getId())
                .type(task.getType())
                .status(task.getStatus())
                .created(task.getCreated())
                .attempts(task.getAttempts())
                .nextAttempt(task.getNextAttempt())
                .lastAttempt(task.getLastAttempt())
                .error(task.getError())
                .build()
        ).toJavaList();
    }

    @GetMapping("/status")
    public Map<String, Integer> status() {
        return dao.fetchStatusnumbers().toJavaMap();
    }

    @GetMapping("/forcerun")
    public String forceRun() {
        core.forceScheduled();
        return "OK";
    }

    @GetMapping("/task/rerun")
    public String runtask(@QueryParam("taskid") String taskid) {
        LOG.warn("Rerun taskid: " + taskid);
        dao.runNow(taskid);
        return "OK";
    }

    @Data
    @Builder
    public static class FailedTask {
        String id;
        TaskType type;
        Status status;
        LocalDateTime created;
        int attempts;
        LocalDateTime nextAttempt;
        LocalDateTime lastAttempt;
        String error;
    }
}
