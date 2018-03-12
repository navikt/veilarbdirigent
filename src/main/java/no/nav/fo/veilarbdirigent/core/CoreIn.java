package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

public class CoreIn {
    private final CoreOut coreOut;
    private final List<MessageHandler> handlers;

    @Inject
    public CoreIn(CoreOut coreOut, List<MessageHandler> handlers) {
        this.coreOut = coreOut;
        this.handlers = handlers;
    }

    @Transactional
    public void submit(Message message) {
        List<Task> tasks = handlers.flatMap((handlers) -> handlers.handle(message));

        saveTasks(tasks);

        coreOut.runActuators();
    }

    private void saveTasks(List<Task> tasks) {
        // TODO Save using DAO
    }
}
