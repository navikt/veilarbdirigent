package no.nav.veilarbdirigent.handlers;

import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.core.api.*;
import no.nav.veilarbdirigent.input.rest.BusyMessage;
import no.nav.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;

public class BusyHandler implements MessageHandler, Actuator<BusyHandler.BusyData, BusyHandler.BusyResult> {

    private final TaskType TYPE = TaskType.of("BUSY_TASK");

    private Core core;

    public BusyHandler(Core core) {
        this.core = core;
    }

    @PostConstruct
    public void register() {
        core.registerHandler(this);
        core.registerActuator(TYPE, this);
    }

    @Override
    public List<Task> handle(Message message) {
        if (message instanceof BusyMessage) {
            BusyMessage msg = (BusyMessage) message;
            return List.of(
                    new Task<>()
                            .withId(String.valueOf(msg.getId()))
                            .withType(TYPE)
                            .withData(new TypedField<>(new BusyData(msg))));

        } else {
            return List.empty();
        }
    }

    @Override
    public Try<BusyResult> handle(BusyData data) {
        return Try.success(new BusyResult(data.busyMessage));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusyData {
        public BusyMessage busyMessage;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusyResult {
        public BusyMessage busyMessage;
    }
}
