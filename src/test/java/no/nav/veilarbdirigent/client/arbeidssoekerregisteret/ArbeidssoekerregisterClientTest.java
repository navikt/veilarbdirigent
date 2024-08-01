package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest(httpPort = 1234)
class ArbeidssoekerregisterClientTest {

    private String apiUrl = "http://localhost:1234"  ;
    private ArbeidssoekerregisterClient arbeidssoekerregisterClient = new ArbeidssoekerregisterClient(apiUrl, () -> "TOKEN");

    @Test
    void skalKunneHenteSamletInformasjonOmArbeidssøker() {
        Fnr fnr = Fnr.of("1234");
        String jsonResponse = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssøker-samletinformasjon.json");
        mockSamletInformasjon(fnr.get(), jsonResponse);
        var samletInformasjon = arbeidssoekerregisterClient.hentSisteSamletInformasjon(fnr);
        assertThat(samletInformasjon.arbeidssoekerperiode()).isNotEmpty();
        assertThat(samletInformasjon.profilering()).isNotEmpty();
    }

    @Test
    void skalKasteFeilHvisServerSvarerMedFlereProfileringer() {
        Fnr fnr = Fnr.of("1234");
        String jsonResponse = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssøker-samletinformasjon-flere-profileringer.json");
        mockSamletInformasjon(fnr.get(), jsonResponse);
        assertThatThrownBy(() -> arbeidssoekerregisterClient.hentSisteSamletInformasjon(fnr)).isInstanceOf(Exception.class);
    }

    @Test
    void skalKasteFeilHvisServerSvarerMedFlereArbeidssøkerperioder() {
        Fnr fnr = Fnr.of("1234");
        String jsonResponse = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssøker-samletinformasjon-flere-arbeidssøkerperioder.json");
        mockSamletInformasjon(fnr.get(), jsonResponse);
        assertThatThrownBy(() -> arbeidssoekerregisterClient.hentSisteSamletInformasjon(fnr)).isInstanceOf(Exception.class);
    }

    @Test
    void skalHåndtereAtDetIkkeFinnesArbeidssøkerperiodeOgProfilering() {
        Fnr fnr = Fnr.of("1234");
        String jsonResponse = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssøker-samletinformasjon-uten-profilering-og-arbeidssøkerperiode.json");
        mockSamletInformasjon(fnr.get(), jsonResponse);
        var samletInformasjon = arbeidssoekerregisterClient.hentSisteSamletInformasjon(fnr);
        assertThat(samletInformasjon.arbeidssoekerperiode()).isEmpty();
        assertThat(samletInformasjon.profilering()).isEmpty();
    }

    @Test
    void testDeserialization() {
        String json = TestUtils.readTestResourceFile("client/arbeidssoekerregisteret/arbeidssøker-samletinformasjon.json");
        ArbeidssoekerregisterClient.SamletInformasjon samletInformasjon = JsonUtils.fromJson(json, ArbeidssoekerregisterClient.SamletInformasjon.class);
        assertThat(samletInformasjon).isNotNull();
    }

    private void mockSamletInformasjon(String fnr, String jsonResponse) {
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