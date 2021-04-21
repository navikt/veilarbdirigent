package no.nav.veilarbdirigent.client.veilarbregistrering;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
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

    public Try<BrukerRegistreringWrapper> hentRegistrering(String fnr) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/registrering?fnr=" + fnr);

        Request request = new Request.Builder()
                .url(url)
                .addHeader(AUTHORIZATION, createBearerToken(serviceTokenSupplier.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            BrukerRegistreringWrapper entity = RestUtils.parseJsonResponse(response, BrukerRegistreringWrapper.class).get();

            return Try.success(entity);
        } catch (Exception e){
            log.warn("Fail request to registrering: " + e);
            return Try.failure(e);
        }
    }
}
