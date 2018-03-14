package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.*;
import no.nav.fo.veilarbdirigent.coreapi.Actuator;
import no.nav.fo.veilarbdirigent.coreapi.Message;
import no.nav.fo.veilarbdirigent.coreapi.MessageHandler;
import no.nav.fo.veilarbdirigent.coreapi.Task;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class FeedHandler implements MessageHandler, Actuator<FeedHandlerData> {
    public static final String FEED_HANDLER = "FEEDHANDLER";

    @Override
    public List<Task> handle(Message message) {
        return List.empty();
    }

    @Override
    public Task<FeedHandlerData> handle(Task<FeedHandlerData> task) {
        FeedHandlerData data = task.getData();
        return null;
    }

    @Override
    public String getType() {
        return FEED_HANDLER;
    }
}
