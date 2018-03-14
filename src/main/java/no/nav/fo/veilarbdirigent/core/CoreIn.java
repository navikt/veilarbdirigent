package no.nav.fo.veilarbdirigent.core;

public interface CoreIn {
    public void submit(Message message);

    public void registerHandler(MessageHandler handler);
}
