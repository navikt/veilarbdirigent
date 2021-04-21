package no.nav.veilarbdirigent.service;

import lombok.RequiredArgsConstructor;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final AktivitetService aktivitetService;

    private final DialogService dialogService;

    public void behandleOppfolgingStartet(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        // TODO: Begge disse må kjøres 1 gang, men ikke mer enn 1 gang
        aktivitetService.opprettAktivteter(oppfolgingStartetKafkaDTO);
        dialogService.opprettDialogForNyregistrertPermitertBruker(oppfolgingStartetKafkaDTO);
    }

}
