package no.nav.fo.veilarbdirigent.input.rest;

import no.nav.fo.veilarbdirigent.core.Core;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/*
   This is a service to create busy tasks. These do nothing and simply validates that the internal flow works
   as expected
 */

@Service
@Path("/busy")
@Produces(MediaType.APPLICATION_JSON)
public class BusyController {

    private final Core core;

    public BusyController(Core core) {
        this.core = core;
    }

    @GET
    @Path("/new")
    public String makeBusyTask() {
        BusyMessage msg = new BusyMessage();
        core.submit(msg);
        return "OK";
    }
}
