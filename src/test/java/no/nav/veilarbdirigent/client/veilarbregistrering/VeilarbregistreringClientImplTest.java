package no.nav.veilarbdirigent.client.veilarbregistrering;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.*;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class VeilarbregistreringClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_lage_riktig_request_og_parse_ordinaer_registrering() {
        String registreringJson = TestUtils.readTestResourceFile("client/veilarbregistrering/ordinaer-registrering-response.json");
        Fnr fnr = Fnr.of("1234");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        VeilarbregistreringClient veilarbregistreringClient = new VeilarbregistreringClientImpl(apiUrl, () -> "TOKEN");

        givenThat(get(urlEqualTo("/api/registrering?fnr=" + fnr))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(registreringJson))
        );

        BrukerRegistreringWrapper brukerRegistreringWrapper = veilarbregistreringClient.hentRegistrering(fnr)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        OrdinaerBrukerRegistrering registrering = brukerRegistreringWrapper.getOrdinaerBrukerRegistrering();

        assertNull(brukerRegistreringWrapper.getSykmeldtBrukerRegistrering());
        assertEquals(BrukerRegistreringType.ORDINAER, brukerRegistreringWrapper.getType());
        assertEquals("2021-05-11T10:40:37.128625", registrering.getOpprettetDato().toString());
        assertEquals("BEHOV_FOR_ARBEIDSEVNEVURDERING", registrering.getProfilering().getInnsatsgruppe());
        assertEquals(DinSituasjonSvar.VIL_FORTSETTE_I_JOBB, registrering.getBesvarelse().getDinSituasjon());
    }

    @Test
    public void skal_lage_riktig_request_og_parse_sykmeldt_registrering() {
        String registreringJson = TestUtils.readTestResourceFile("client/veilarbregistrering/sykmeldt-registrering-response.json");
        Fnr fnr = Fnr.of("1234");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        VeilarbregistreringClient veilarbregistreringClient = new VeilarbregistreringClientImpl(apiUrl, () -> "TOKEN");

        givenThat(get(urlEqualTo("/api/registrering?fnr=" + fnr))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(registreringJson))
        );

        BrukerRegistreringWrapper brukerRegistreringWrapper = veilarbregistreringClient.hentRegistrering(fnr)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        SykmeldtBrukerRegistrering registrering = brukerRegistreringWrapper.getSykmeldtBrukerRegistrering();

        assertNull(brukerRegistreringWrapper.getOrdinaerBrukerRegistrering());
        assertEquals(BrukerRegistreringType.SYKMELDT, brukerRegistreringWrapper.getType());
        assertEquals("2021-05-11T10:40:37.128625", registrering.getOpprettetDato().toString());
        assertEquals(FremtidigSituasjonSvar.NY_ARBEIDSGIVER, registrering.getBesvarelse().getFremtidigSituasjon());
    }

}
