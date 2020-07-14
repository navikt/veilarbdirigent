package no.nav.veilarbdirigent.output.services;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.output.domain.BrukerRegistreringWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;
import static no.nav.veilarbdirigent.utils.SerializerUtils.mapper;
import static org.slf4j.LoggerFactory.getLogger;

@Slf4j
public class VeilarbregisteringService {
    public static final String VEILARBREGISTRERINGAPI_URL_PROPERTY = "VEILARBREGISTRERINGAPI_URL";
    private final String host;
    private static final Logger LOG = getLogger(VeilarbregisteringService.class);

    private final OkHttpClient client;
    private final AktorregisterClient aktorService;

    public VeilarbregisteringService(OkHttpClient client, AktorregisterClient aktorService) {
        this.client = client;
        this.aktorService = aktorService;
        Supplier<String> naisUrl = () -> UrlUtils.clusterUrlForApplication("veilarbregistrering") + "/veilarbregistrering/api";
        this.host = getOptionalProperty(VEILARBREGISTRERINGAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    public Try<BrukerRegistreringWrapper> hentRegistrering(String aktorId) {
        String fnr = aktorService.hentFnr(aktorId);
        String url = String.format("%s/registrering?fnr=%s", host, fnr);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try{
            var resp = client.newCall(request).execute();
            var body = resp.body().string();

            LOG.warn("reg info: " + body);
            return Try.success(mapper.readValue(body, BrukerRegistreringWrapper.class));
        }
        catch (Exception e){
            log.warn("Fail request to registrering: " + e);
            return Try.failure(e);
        }
    }
}
