package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;

public class CoreIn {
    private final CoreOut coreOut;
    private final TaskDAO taskDAO;
    private final List<MessageHandler> handlers;

    public CoreIn(CoreOut coreOut, TaskDAO taskDAO, List<MessageHandler> handlers) {
        this.coreOut = coreOut;
        this.taskDAO = taskDAO;
        this.handlers = handlers;
    }

    public void submit(Message message) {
        List<Task> tasks = handlers.flatMap((handler) -> handler.handle(message));

        taskDAO.insert(tasks);

        coreOut.runActuators();
    }
}
