package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AktivitetService {

    private final VeilarbaktivitetClient veilarbaktivitetClient;

    private final VeilarbmalverkClient veilarbmalverkClient;

    public Try<String> opprettAktivitetForBrukerMedMal(AktorId aktorId, String malName) {
        return veilarbmalverkClient
                .hentMal(malName)
                .flatMap((template) -> veilarbaktivitetClient.lagAktivitet(aktorId, template));
    }

}
