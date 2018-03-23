package no.nav.fo.veilarbdirigent.rest;

import no.nav.fo.veilarbdirigent.core.api.Task;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
    @Inject
    private TaskDAO dao;

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
