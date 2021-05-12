package no.nav.veilarbdirigent.utils;

import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;

import java.util.List;
import java.util.Optional;

public class OppfolgingsperiodeUtils {

    public static Optional<Oppfolgingsperiode> hentGjeldendeOppfolgingsperiode(List<Oppfolgingsperiode> oppfolgingsperioder) {
        return oppfolgingsperioder.stream().filter(op -> op.getSluttDato() == null).findFirst();
    }

}
