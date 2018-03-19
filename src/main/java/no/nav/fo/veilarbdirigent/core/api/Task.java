package no.nav.fo.veilarbdirigent.core.api;

import lombok.*;
import lombok.experimental.Wither;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
@Builder
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
