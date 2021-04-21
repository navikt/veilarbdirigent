package no.nav.veilarbdirigent.client.veilarbregistrering;

import io.vavr.control.Try;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;

public interface VeilarbregistreringClient {

    Try<BrukerRegistreringWrapper> hentRegistrering(Fnr fnr);

}
