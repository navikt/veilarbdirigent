package no.nav.fo.veilarbdirigent.core;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class Task {
    String id;
    String type;
    Status status;
    LocalDateTime created;
    int attempts;
    LocalDateTime nextAttempt;
    LocalDateTime lastAttempt;
    String data;
    String error;
}
