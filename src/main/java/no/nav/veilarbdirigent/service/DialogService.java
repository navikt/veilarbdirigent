package no.nav.veilarbdirigent.service;

import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.DinSituasjonSvar;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DialogService {

    private final static String permitertJson = "{\n" +
            "\"overskrift\": \"Permittering – automatisk melding fra NAV\",\n" +
            "\"tekst\": \"Hei!\\n" +
            "Ha tett kontakt med arbeidsgiveren din om situasjonen fremover, nå når du er permittert. Når du har begynt i jobben din igjen, eller mister jobben, så [gir du beskjed til NAV slik](https://www.nav.no/arbeid/no/dagpenger/#gi-beskjed-hvis-situasjonen-din-endrer-seg).\\n" +
            "Du finner informasjon om [dagpenger og permittering her](https://www.nav.no/arbeid/no/permittert).\\n" +
            "Hilsen NAV\"\n" +
            "}";

    private final static List<String> REGISTERING_FORSLAG = List.of(
            "STANDARD_INNSATS",
            "SITUASJONSBESTEMT_INNSATS",
            "BEHOV_FOR_ARBEIDSEVNEVURDERING"
    );

    private final VeilarbdialogClient veilarbdialogClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final AktorOppslagClient aktorOppslagClient;

    public void opprettDialogForNyregistrertPermitertBruker(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {

        boolean erNyRegistrert = REGISTERING_FORSLAG.contains("TODO: Innsatsgruppe fra bruker");

        if (!erNyRegistrert) {
           return;
        }

        Fnr fnr = aktorOppslagClient.hentFnr(oppfolgingStartetKafkaDTO.getAktorId());

        BrukerRegistreringWrapper registeringsData = veilarbregistreringClient.hentRegistrering(fnr)
                .getOrElseThrow(() -> new RuntimeException());

        // TODO: Null check
        DinSituasjonSvar svar = registeringsData.getRegistrering().getBesvarelse().getDinSituasjon();

        if (DinSituasjonSvar.ER_PERMITTERT.equals(svar)){
            veilarbdialogClient.lagDialog(oppfolgingStartetKafkaDTO.getAktorId(), permitertJson);
        }
    }


}
