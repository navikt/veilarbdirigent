package no.nav.fo.veilarbdirigent.admin;

import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.api.Task;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Service
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminController {
    private final Core core;
    private final TaskDAO dao;

    public AdminController(Core core, TaskDAO dao) {
        this.core = core;
        this.dao = dao;
    }

    @GET
    public List<Task> failedTasks() {
        return dao.fetchAllFailedTasks().toJavaList();
    }

    @GET
    @Path("/status")
    public Map<String, Integer> status() {
        return dao.fetchStatusnumbers().toJavaMap();
    }

    @POST
    @Path("/forcerun")
    public String forceRun() {
        core.forceScheduled();
        return "OK";
    }

    @POST
    @Path("/task/{taskid}/rerun")
    public int runtask(@PathParam("taskid") String taskId) {
        return dao.runNow(taskId);
    }
}
