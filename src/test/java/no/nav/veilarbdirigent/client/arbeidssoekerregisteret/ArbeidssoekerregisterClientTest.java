package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest(httpPort = 1234)
class ArbeidssoekerregisterClientTest {

    private String apiUrl = "http://localhost:1234"  ;
    private ArbeidssoekerregisterClient arbeidssoekerregisterClient = new ArbeidssoekerregisterClient(apiUrl, () -> "TOKEN");

    @Test
    void skalKunneHenteSamletInformasjonOmArbeidssøker() {
        Fnr fnr = Fnr.of("1234");
        mockSamletInformasjon(fnr.get());
        var samletInformasjon = arbeidssoekerregisterClient.hentSisteSamletInformasjon(fnr);
        assertThat(samletInformasjon).isNotNull();
    }

    @Test
    void testDeserialization() {
        String json = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssøker-samletinformasjon.json");
        ArbeidssoekerregisterClient.SamletInformasjon samletInformasjon = JsonUtils.fromJson(json, ArbeidssoekerregisterClient.SamletInformasjon.class);
        assertThat(samletInformasjon).isNotNull();
    }

    private void mockSamletInformasjon(String fnr) {
        String jsonResponse = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssøker-samletinformasjon.json");
        givenThat(post(urlEqualTo("/api/v1/veileder/samlet-informasjon?siste=true"))
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
}