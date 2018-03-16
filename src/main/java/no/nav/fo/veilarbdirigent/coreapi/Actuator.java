package no.nav.fo.veilarbdirigent.coreapi;

public interface Actuator<DATA, RESULT> {
    public Task<DATA, RESULT> handle(Task<DATA, RESULT> task);
    public String getType();
}
