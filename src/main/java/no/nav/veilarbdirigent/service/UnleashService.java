package no.nav.veilarbdirigent.service;

import lombok.RequiredArgsConstructor;
import no.nav.common.featuretoggle.UnleashClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnleashService {

    private final static String IS_KAFKA_ENABLED_TOGGLE = "veilarbdirigent.is_kafka_enabled";

    private final UnleashClient unleashClient;

    public boolean isKafkaEnabled() {
        return unleashClient.isEnabled(IS_KAFKA_ENABLED_TOGGLE);
    }

}
