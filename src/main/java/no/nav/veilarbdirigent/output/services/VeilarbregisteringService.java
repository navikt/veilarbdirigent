package no.nav.veilarbdirigent.output.services;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.AktorId;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.output.domain.BrukerRegistreringWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.function.Supplier;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;

@Slf4j
public class VeilarbregisteringService {
    public static final String VEILARBREGISTRERINGAPI_URL_PROPERTY = "VEILARBREGISTRERINGAPI_URL";
    private final String host;

    private final OkHttpClient client;
    private final AktorregisterClient aktorService;

    public VeilarbregisteringService(OkHttpClient client, AktorregisterClient aktorService) {
        this.client = client;
        this.aktorService = aktorService;
        Supplier<String> naisUrl = () -> UrlUtils.clusterUrlForApplication("veilarbregistrering") + "/veilarbregistrering/api";
        this.host = getOptionalProperty(VEILARBREGISTRERINGAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    public Try<BrukerRegistreringWrapper> hentRegistrering(String aktorId) {
        String fnr = aktorService.hentFnr(AktorId.of(aktorId)).get();
        String url = String.format("%s/registrering?fnr=%s", host, fnr);

        Request request = new Request.Builder()
                .url(url)
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
