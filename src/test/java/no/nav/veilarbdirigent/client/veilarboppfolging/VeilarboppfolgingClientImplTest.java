package no.nav.veilarbdirigent.client.veilarboppfolging;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.UUID;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.TestUtils;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VeilarboppfolgingClientImplTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Test
    public void skal_lage_riktig_request_og_parse_oppfolgingsperioder() {
        String oppfolgingsperioderJson = TestUtils.readTestResourceFile("client/veilarboppfolging/oppfolgingsperioder-response.json");
        Fnr fnr = Fnr.of("1234");
        String apiUrl = "http://localhost:" + wireMock.getPort();
        VeilarboppfolgingClient veilarboppfolgingClient = new VeilarboppfolgingClientImpl(apiUrl, () -> "TOKEN");

        wireMock.stubFor(post(urlEqualTo("/veilarboppfolging/api/v3/oppfolging/hent-perioder"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(oppfolgingsperioderJson))
        );

        List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);

        assertEquals(1, oppfolgingsperioder.size());

        Oppfolgingsperiode oppfolgingsperiode = oppfolgingsperioder.get(0);

        assertEquals("aktorId123", oppfolgingsperiode.getAktorId());
        assertEquals(UUID.fromString("6aaadded-6d6a-4962-ae38-c9b4664a8d8c"), oppfolgingsperiode.getUuid());
        assertEquals("2021-05-11T10:40:37+02:00", oppfolgingsperiode.getStartDato().toString());
        assertNull(oppfolgingsperiode.getSluttDato());
    }

}


