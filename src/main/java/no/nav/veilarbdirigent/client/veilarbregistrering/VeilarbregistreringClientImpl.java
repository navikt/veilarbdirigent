package no.nav.veilarbdirigent.client.veilarbregistrering;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringType;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.OrdinaerBrukerRegistrering;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.SykmeldtBrukerRegistrering;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.function.Supplier;

import static no.nav.common.rest.client.RestUtils.createBearerToken;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class VeilarbregistreringClientImpl implements VeilarbregistreringClient {

    private final String apiUrl;

    private final Supplier<String> serviceTokenSupplier;

    private final OkHttpClient client;

    public VeilarbregistreringClientImpl(String apiUrl, Supplier<String> serviceTokenSupplier) {
        this.apiUrl = apiUrl;
        this.serviceTokenSupplier = serviceTokenSupplier;
        this.client = RestClient.baseClient();
    }

    public Try<BrukerRegistreringWrapper> hentRegistrering(Fnr fnr) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/registrering?fnr=" + fnr);

        Request request = new Request.Builder()
                .url(url)
                .addHeader(AUTHORIZATION, createBearerToken(serviceTokenSupplier.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);

            String json = RestUtils.getBodyStr(response).orElseThrow();
            JsonNode jsonNode = JsonUtils.getMapper().readTree(json);

            BrukerRegistreringType registreringType = BrukerRegistreringType.valueOf(jsonNode.get("type").textValue());

            BrukerRegistreringWrapper wrapper = new BrukerRegistreringWrapper();
            wrapper.setType(registreringType);

            if (BrukerRegistreringType.SYKMELDT.equals(registreringType)) {
                SykmeldtBrukerRegistrering sykmeldtBrukerRegistrering = JsonUtils.fromJson(
                        jsonNode.get("registrering").toString(), SykmeldtBrukerRegistrering.class
                );

                wrapper.setSykmeldtBrukerRegistrering(sykmeldtBrukerRegistrering);
            } else {
                OrdinaerBrukerRegistrering ordinaerBrukerRegistrering = JsonUtils.fromJson(
                        jsonNode.get("registrering").toString(), OrdinaerBrukerRegistrering.class
                );

                wrapper.setOrdinaerBrukerRegistrering(ordinaerBrukerRegistrering);
            }

            return Try.success(wrapper);
        } catch (Exception e){
            log.warn("Fail request to registrering: " + e);
            return Try.failure(e);
        }
    }
}
