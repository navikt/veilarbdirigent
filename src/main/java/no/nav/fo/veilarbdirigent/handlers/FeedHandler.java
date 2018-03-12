package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Message;
import no.nav.fo.veilarbdirigent.core.MessageHandler;
import no.nav.fo.veilarbdirigent.core.Task;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;

public class FeedHandler implements MessageHandler {
    @Inject
    JdbcTemplate jdbc;

    @Override
    public List<Task> handle(Message message) {
        return null;
    }
}
