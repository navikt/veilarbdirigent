package no.nav.veilarbdirigent.client.veilarbregistrering;

import io.vavr.control.Try;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;

import java.util.Optional;

public interface VeilarbregistreringClient {

    Try<Optional<BrukerRegistreringWrapper>> hentRegistrering(Fnr fnr);

}
