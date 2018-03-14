package no.nav.fo.veilarbdirigent.coreapi;

public interface Actuator<T> {
    public Task<T> handle(Task<T> task);
    public String getType();
}
