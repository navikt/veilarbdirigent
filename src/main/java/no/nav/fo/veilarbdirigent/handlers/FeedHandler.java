package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.coreapi.Actuator;
import no.nav.fo.veilarbdirigent.coreapi.Message;
import no.nav.fo.veilarbdirigent.coreapi.MessageHandler;
import no.nav.fo.veilarbdirigent.coreapi.Task;

public class FeedHandler implements MessageHandler, Actuator<FeedHandlerData, FeedHandlerResult> {
    public static final String FEED_HANDLER = "FEEDHANDLER";

    @Override
    public List<Task> handle(Message message) {
        return List.empty();
    }

    @Override
    public Task<FeedHandlerData, FeedHandlerResult> handle(Task<FeedHandlerData, FeedHandlerResult> task) {
        FeedHandlerData data = task.getData();
        return null;
    }

    @Override
    public String getType() {
        return FEED_HANDLER;
    }
}
