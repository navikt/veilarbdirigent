package no.nav.veilarbdirigent.admin;

import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.core.api.Task;
import no.nav.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final Core core;
    private final TaskDAO dao;

    public AdminController(Core core, TaskDAO dao) {
        this.core = core;
        this.dao = dao;
    }

    @GetMapping
    public List<Task> failedTasks() {
        return dao.fetchAllFailedTasks().toJavaList();
    }

    @GetMapping("/status")
    public Map<String, Integer> status() {
        return dao.fetchStatusnumbers().toJavaMap();
    }

    @PostMapping("/forcerun")
    public String forceRun() {
        core.forceScheduled();
        return "OK";
    }

    @PostMapping("/task/{taskid}/rerun")
    public int runtask(@PathVariable("taskid") String taskId) {
        return dao.runNow(taskId);
    }
}
