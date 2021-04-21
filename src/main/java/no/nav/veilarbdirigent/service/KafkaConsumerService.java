package no.nav.veilarbdirigent.service;

import lombok.RequiredArgsConstructor;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final VeilarbaktivitetClient veilarbaktivitetClient;

    private final VeilarbmalverkClient veilarbmalverkClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final VeilarbdialogClient veilarbdialogClient;

    public void behandleOppfolgingStartet(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        // TODO
    }

}
