package no.nav.veilarbdirigent.core.api;

import io.vavr.control.Try;

public interface Actuator<DATA, RESULT> {
    public Try<RESULT> handle(DATA data);
}
