package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class FeedHandler implements MessageHandler {
    public static final String FEED_HANDLER = "FEEDHANDLER";

    @Inject
    JdbcTemplate jdbc;

    @Inject
    Core core;

    @PostConstruct
    public void registerHandler() {
        core.registerHandler(this);
        core.registerActuator(FEED_HANDLER, new FeedHandlerDefinition());
    }

    @Override
    public List<Task> handle(Message message) {
        return List.empty();
    }

    private class FeedHandlerDefinition implements Actuator<FeedHandlerData> {
        @Override
        public Task<FeedHandlerData> handle(Task<FeedHandlerData> task) {
            return null;
        }
    }
}
