package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.TestUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


class ArbeidssoekerregisterClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    private String apiUrl = "http://localhost:" + wireMockRule.port();
    private ArbeidssoekerregisterClient arbeidssoekerregisterClient = new ArbeidssoekerregisterClient(apiUrl, () -> "TOKEN");

    @Test
    void testHentingAvArbeidssøkerperioder() {
        Fnr fnr = Fnr.of("1234");
        var arbeidsoekerPerioder = arbeidssoekerregisterClient.hentArbeidsoekerPerioder(fnr);
        assertThat(arbeidsoekerPerioder).isNotEmpty();
    }

    @Test
    void testDeserialization() {
        String json = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssoekerperioder.json");
        List<ArbeidssoekerregisterClient.ArbeidssoekerPeriodeResponse> arbeidssoekerPeriodeResponses = JsonUtils.fromJsonArray(json, ArbeidssoekerregisterClient.ArbeidssoekerPeriodeResponse.class);
        assertThat(arbeidssoekerPeriodeResponses).isNotEmpty();
    }

    private void mockAvArbeidssøkerperioder(String fnr) {
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
                        .withBody("""
                            [
                              {
                                "periodeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                "startet": {
                                  "tidspunkt": "2021-09-29T11:22:33.444Z",
                                  "utfoertAv": {
                                    "type": "UKJENT_VERDI",
                                    "id": "12345678910"
                                  },
                                  "kilde": "string",
                                  "aarsak": "string",
                                  "tidspunktFraKilde": {
                                    "tidspunkt": "2021-09-29T11:20:33.444Z",
                                    "avviksType": "UKJENT_VERDI"
                                  }
                                },
                                "avsluttet": {
                                  "tidspunkt": "2021-09-29T11:22:33.444Z",
                                  "utfoertAv": {
                                    "type": "UKJENT_VERDI",
                                    "id": "12345678910"
                                  },
                                  "kilde": "string",
                                  "aarsak": "string",
                                  "tidspunktFraKilde": {
                                    "tidspunkt": "2021-09-29T11:20:33.444Z",
                                    "avviksType": "UKJENT_VERDI"
                                  }
                                }
                              }
                            ]
                        """)
                )
        );
    }
}