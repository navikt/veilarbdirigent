package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import no.nav.veilarbdirigent.core.api.Task;
import org.springframework.stereotype.Service;

@Service
public class TaskProcessorService {

    public Try<String> processTask(Task task) {
        return Try.failure(new RuntimeException());
    }

}
