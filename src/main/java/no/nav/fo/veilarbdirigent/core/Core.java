package no.nav.fo.veilarbdirigent.core;

public class Core implements CoreIn, CoreOut {
    private final CoreIn coreIn;
    private final CoreOut coreOut;

    public Core(CoreIn coreIn, CoreOut coreOut) {
        this.coreIn = coreIn;
        this.coreOut = coreOut;
    }

    public void submit(Message message) {
        this.coreIn.submit(message);
    }

    public void runActuators() {
        this.coreOut.runActuators();
    }

    public void registerHandler(MessageHandler handler) {
        this.coreIn.registerHandler(handler);
    }

    public void registerActuator(String name, Actuator<?> taskRunner) {
        this.coreOut.registerActuator(name, taskRunner);
    }
}
