package no.nav.veilarbdirigent.client.veilarboppfolging;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;

import java.util.List;

public interface VeilarboppfolgingClient {

    List<Oppfolgingsperiode> hentOppfolgingsperioder(Fnr fnr);

}
