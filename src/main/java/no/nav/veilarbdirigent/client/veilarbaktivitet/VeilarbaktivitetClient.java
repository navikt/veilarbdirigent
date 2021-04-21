package no.nav.veilarbdirigent.client.veilarbaktivitet;

import io.vavr.control.Try;
import no.nav.common.types.identer.AktorId;

public interface VeilarbaktivitetClient {

    Try<String> lagAktivitet(AktorId aktorId, String data);

}
