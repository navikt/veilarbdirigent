package no.nav.fo.veilarbdirigent.admin;

import no.nav.fo.veilarbdirigent.core.api.Task;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Service
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminController {
    private final TaskDAO dao;

    public AdminController(TaskDAO dao) {
        this.dao = dao;
    }

    @GET
    public List<Task> data() {
        return dao.fetchAllFailedTasks().toJavaList();
    }

    @GET
    @Path("/status")
    public Map<String, Integer> status() {
        return dao.fetchStatusnumbers().toJavaMap();
    }
}
