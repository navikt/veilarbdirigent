package no.nav.veilarbdirigent.unleash;

import lombok.RequiredArgsConstructor;
import no.nav.common.featuretoggle.UnleashClient;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class KafkaAivenUnleash implements Supplier<Boolean> {
    private final UnleashClient unleashClient;

    private static final String AIVEN_KAFKA_DISABLED = "veilarbdirigent.kafka.aiven.consumer.disabled";
    @Override
    public Boolean get() {
        return unleashClient.isEnabled(AIVEN_KAFKA_DISABLED);
    }
}
