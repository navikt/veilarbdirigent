package no.nav.veilarbdirigent.repository.domain;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@Builder
public class Task {
    String id;
    TaskType type;
    TaskStatus taskStatus;
    LocalDateTime created;
    int attempts;
    LocalDateTime nextAttempt;
    LocalDateTime lastAttempt;
    String jsonData;
    String jsonResult;
    String error;

    @Override
    public String toString() {
        return String.format("Task{ id=%s, type=%s status=%s }", id, type.getType(), taskStatus);
    }

}
