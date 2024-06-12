package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest(httpPort = 1234)
class ArbeidssoekerregisterClientTest {

    private String apiUrl = "http://localhost:1234"  ;
    private ArbeidssoekerregisterClient arbeidssoekerregisterClient = new ArbeidssoekerregisterClient(apiUrl, () -> "TOKEN");

    @Test
    void skalKunneHenteArbeidssøkerperioder() {
        Fnr fnr = Fnr.of("1234");
        mockAvArbeidssøkerperioder(fnr.get());
        var arbeidsøkerPerioder = arbeidssoekerregisterClient.hentArbeidsoekerPerioder(fnr);
        assertThat(arbeidsøkerPerioder).hasSize(1);
    }

    @Test
    void skalKunneHenteProfileringer() {
        Fnr fnr = Fnr.of("1234");
        UUID arbeidssøkerperiodeId = UUID.randomUUID();
        mockAvProfilering(fnr.get(), arbeidssøkerperiodeId);

        var profileringer = arbeidssoekerregisterClient.hentProfileringer(fnr, arbeidssøkerperiodeId);

        assertThat(profileringer).hasSize(1);
    }

    @Test
    void testDeserialization() {
        String json = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssoekerperioder.json");
        List<ArbeidssoekerregisterClient.ArbeidssoekerPeriodeResponse> arbeidssoekerPeriodeResponses = JsonUtils.fromJsonArray(json, ArbeidssoekerregisterClient.ArbeidssoekerPeriodeResponse.class);
        assertThat(arbeidssoekerPeriodeResponses).isNotEmpty();
    }

    private void mockAvArbeidssøkerperioder(String fnr) {
        String jsonResponse = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssoekerperioder.json");
        givenThat(post(urlEqualTo("/api/v1/veileder/arbeidssoekerperioder"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer TOKEN"))
                .withRequestBody(equalToJson(String.format("""
                    {
                      "identitetsnummer": "%s"
                    }
                    """, fnr)
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(jsonResponse)
                )
        );
    }

    private void mockAvProfilering(String fnr, UUID arbeidssøkerperiode) {
        String jsonResponse = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/profileringer.json");
        givenThat(post(urlEqualTo("/api/v1/veileder/profilering"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer TOKEN"))
                .withRequestBody(equalToJson(String.format("""
                        {
                           "identitetsnummer": "%s",
                           "periodeId": "%s"
                         }
                        """, fnr, arbeidssøkerperiode)
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(jsonResponse))
                );
    }
}