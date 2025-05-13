package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AktivitetService {

    private final VeilarbaktivitetClient veilarbaktivitetClient;
    private final VeilarboppfolgingClient veilarboppfolgingClient;
    private final AktorOppslagClient aktorOppslagClient;
    private final AktivitetTemplateProviderService aktivitetTemplateProviderService;

    public Try<String> opprettAktivitetForBrukerMedMal(AktorId aktorId, String malName) {
        var gjeldendeOppfolgingsPeriode = veilarboppfolgingClient.hentOppfolgingsperioder(aktorOppslagClient.hentFnr(aktorId))
                .stream().filter(periode -> periode.getSluttDato() == null).findFirst();
        return gjeldendeOppfolgingsPeriode
                .map((periode) -> veilarbaktivitetClient
                        .lagAktivitet(aktivitetTemplateProviderService.getCvJobbprofilAktivitetMal(), periode.getUuid())
                )
                .orElse(Try.failure(new IllegalStateException("Ingen åpne perioder (bruker ikke under oppfolging)")));
    }

}
