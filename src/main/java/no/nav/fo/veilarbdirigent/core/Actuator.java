package no.nav.fo.veilarbdirigent.core;

public interface Actuator<T> {
    public Task<T> handle(Task<T> task);
}
