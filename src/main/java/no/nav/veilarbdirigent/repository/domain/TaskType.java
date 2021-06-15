package no.nav.veilarbdirigent.repository.domain;

import lombok.Value;

@Value
public class TaskType {
    String type;

    public static TaskType of(String type) {
        return new TaskType(type);
    }
}
