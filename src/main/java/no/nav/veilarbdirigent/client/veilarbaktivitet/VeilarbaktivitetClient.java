package no.nav.veilarbdirigent.client.veilarbaktivitet;

import io.vavr.control.Try;
import no.nav.common.types.identer.AktorId;

import java.util.UUID;

public interface VeilarbaktivitetClient {

    Try<String> lagAktivitet(String data, UUID oppfolgingsPeriodeId);

}
