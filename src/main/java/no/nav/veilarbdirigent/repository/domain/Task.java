package no.nav.veilarbdirigent.repository.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
@Builder
public class Task {
    String id;
    TaskType type;
    Status status;
    LocalDateTime created;
    int attempts;
    LocalDateTime nextAttempt;
    LocalDateTime lastAttempt;
    String jsonData;
    String jsonResult;
    String error;
}
