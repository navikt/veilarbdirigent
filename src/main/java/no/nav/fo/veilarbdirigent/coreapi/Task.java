package no.nav.fo.veilarbdirigent.coreapi;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.LocalDateTime;

@Value
@Builder
@Wither
public class Task<DATA, RESULT> {
    String id;
    String type;
    Status status;
    LocalDateTime created;
    int attempts;
    LocalDateTime nextAttempt;
    LocalDateTime lastAttempt;
    DATA data;
    RESULT result;
    String error;
}
