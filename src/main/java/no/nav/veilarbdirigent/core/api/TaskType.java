package no.nav.veilarbdirigent.core.api;

import lombok.Value;

@Value
public class TaskType {
    String type;

    public static TaskType of(String type) {
        return new TaskType(type);
    }
}
