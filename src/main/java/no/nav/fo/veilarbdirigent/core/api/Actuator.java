package no.nav.fo.veilarbdirigent.core.api;

import io.vavr.control.Either;

public interface Actuator<DATA, RESULT> {
    public Either<String, Task<DATA, RESULT>> handle(Task<DATA, RESULT> task);
}
