package no.nav.veilarbdirigent.service;

import lombok.RequiredArgsConstructor;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final UnleashService unleashService;

    public void behandleOppfolgingStartet(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        if (!unleashService.isKafkaEnabled()) {
            throw new RuntimeException("Kafka toggle is not enabled");
        }

        // TODO
    }

}
