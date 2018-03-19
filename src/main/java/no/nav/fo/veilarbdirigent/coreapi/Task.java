package no.nav.fo.veilarbdirigent.coreapi;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import java.time.LocalDateTime;

@Value
@Builder
@Wither
public class Task<DATA, RESULT> {
    String id;
    TaskType type;
    Status status;
    LocalDateTime created;
    int attempts;
    LocalDateTime nextAttempt;
    LocalDateTime lastAttempt;
    TypedField<DATA> data;
    TypedField<RESULT> result;
    String error;
}
