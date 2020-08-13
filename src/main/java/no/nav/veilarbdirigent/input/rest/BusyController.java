package no.nav.veilarbdirigent.input.rest;

import no.nav.veilarbdirigent.core.Core;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
   This is a service to create busy tasks. These do nothing and simply validates that the internal flow works
   as expected
 */

@RestController
@RequestMapping("/busy")
public class BusyController {

    private final Core core;

    public BusyController(Core core) {
        this.core = core;
    }

    @PostMapping("/new")
    public String makeBusyTask() {
        BusyMessage msg = new BusyMessage();
        core.submit(msg);
        return "OK";
    }
}
