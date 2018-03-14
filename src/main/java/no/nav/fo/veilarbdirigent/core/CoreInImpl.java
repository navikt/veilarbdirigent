package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;

public class CoreInImpl implements CoreIn {
    private final CoreOut coreOut;
    private final TaskDAO taskDAO;
    private List<MessageHandler> handlers = List.empty();

    public CoreInImpl(CoreOut coreOut, TaskDAO taskDAO) {
        this.coreOut = coreOut;
        this.taskDAO = taskDAO;
    }

    public void submit(Message message) {
        List<Task> tasks = handlers.flatMap((handler) -> handler.handle(message));

        taskDAO.insert(tasks);

        coreOut.runActuators();
    }

    public void registerHandler(MessageHandler handler) {
        this.handlers = this.handlers.append(handler);
    }
}
