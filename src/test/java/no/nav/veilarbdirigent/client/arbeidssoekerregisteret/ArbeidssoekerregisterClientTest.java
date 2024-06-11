package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import no.nav.common.json.JsonUtils;
import no.nav.veilarbdirigent.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class ArbeidssoekerregisterClientTest {
    @Test
    void testDeserialization() {
        String json = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssoekerperioder.json");
        List<ArbeidssoekerregisterClient.ArbeidssoekerPeriodeResponse> arbeidssoekerPeriodeResponses = JsonUtils.fromJsonArray(json, ArbeidssoekerregisterClient.ArbeidssoekerPeriodeResponse.class);
        Assertions.assertThat(arbeidssoekerPeriodeResponses).isNotEmpty();
    }

}