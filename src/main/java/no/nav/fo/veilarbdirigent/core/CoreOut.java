package no.nav.fo.veilarbdirigent.core;

public interface CoreOut {
    public void runActuators();

    public void registerActuator(String name, Actuator<?> taskRunner);
}
