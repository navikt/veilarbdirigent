package no.nav.fo.veilarbdirigent.core.api;

import io.vavr.collection.List;

public interface MessageHandler {
    public List<Task> handle(Message message);
}
