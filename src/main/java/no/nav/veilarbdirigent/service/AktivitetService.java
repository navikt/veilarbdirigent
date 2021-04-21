package no.nav.veilarbdirigent.service;

import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AktivitetService {

    public final static String CV_JOBBPROFIL_MAL_NAME = "cv_jobbprofil_aktivitet";

    public final static String JOBBSOKERKOMPETANSE_MAL_NAME = "jobbsokerkompetanse_aktivitet";

    private final static List<String> REGISTERING_FORSLAG = List.of(
            "STANDARD_INNSATS",
            "SITUASJONSBESTEMT_INNSATS",
            "BEHOV_FOR_ARBEIDSEVNEVURDERING"
    );

    private final static List<String> SYKMELDT_BRUKER_TYPER = List.of("SKAL_TIL_NY_ARBEIDSGIVER");

    private final VeilarbaktivitetClient veilarbaktivitetClient;

    private final VeilarbmalverkClient veilarbmalverkClient;

    public void opprettAktivteter(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        // TODO: Implement
//        OppfolgingDataFraFeed msg = (OppfolgingDataFraFeed) message;
//
//        boolean erNySykmeldtBrukerRegistrert = SYKMELDT_BRUKER_TYPER.contains(msg.getSykmeldtBrukerType());
//        boolean erNyRegistrert = REGISTERING_FORSLAG.contains(msg.getForeslattInnsatsgruppe());
//
//        if (erNySykmeldtBrukerRegistrert || erNyRegistrert) {
//          TODO: Opprett aktiviteter
//        }
    }

    private void opprettCvJobbprofilAktivitet(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        String mal = veilarbmalverkClient.hentMal(CV_JOBBPROFIL_MAL_NAME).getOrElseThrow(() -> new RuntimeException());
        veilarbaktivitetClient.lagAktivitet(oppfolgingStartetKafkaDTO.getAktorId(), mal);
    }

    private void opprettJobbsokerkompetanseAktivitet(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        String mal = veilarbmalverkClient.hentMal(JOBBSOKERKOMPETANSE_MAL_NAME).getOrElseThrow(() -> new RuntimeException());
        veilarbaktivitetClient.lagAktivitet(oppfolgingStartetKafkaDTO.getAktorId(), mal);
    }

}
