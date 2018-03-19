package no.nav.fo.veilarbdirigent.core.api;

public interface Actuator<DATA, RESULT> {
    public Task<DATA, RESULT> handle(Task<DATA, RESULT> task);
    public TaskType getType();
}
