package no.nav.fo.veilarbdirigent.core;

import io.vavr.collection.List;

public interface MessageHandler {
    public List<Task> handle(Message message);
}
