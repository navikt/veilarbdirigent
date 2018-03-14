package no.nav.fo.veilarbdirigent.coreapi;

import io.vavr.collection.List;

public interface MessageHandler {
    public List<Task> handle(Message message);
}
