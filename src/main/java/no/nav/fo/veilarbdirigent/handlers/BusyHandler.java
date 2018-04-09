package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.api.*;
import no.nav.fo.veilarbdirigent.input.rest.BusyMessage;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class BusyHandler implements MessageHandler, Actuator<BusyHandler.BusyData, BusyHandler.BusyResult> {

    private final TaskType TYPE = TaskType.of("BUSY_TASK");

    @Inject
    Core core;

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
