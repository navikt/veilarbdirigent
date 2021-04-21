package no.nav.veilarbdirigent.client.veilarbaktivitet;

import io.vavr.control.Try;

public interface VeilarbaktivitetClient {

    Try<String> lagAktivitet(String aktorId, String data);

}
