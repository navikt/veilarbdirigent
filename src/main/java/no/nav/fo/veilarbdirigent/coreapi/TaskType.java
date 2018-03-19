package no.nav.fo.veilarbdirigent.coreapi;

import lombok.Value;

@Value
public class TaskType {
    String type;

    public static TaskType of(String type) {
        return new TaskType(type);
    }
}
