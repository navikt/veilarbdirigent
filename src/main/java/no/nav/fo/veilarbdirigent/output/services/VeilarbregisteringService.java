package no.nav.fo.veilarbdirigent.output.services;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.util.UrlUtils;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbdirigent.output.domain.BrukerRegistreringWrapper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.util.function.Supplier;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Service
@Slf4j
public class VeilarbregisteringService {
    public static final String VEILARBREGISTRERINGAPI_URL_PROPERTY = "VEILARBREGISTRERINGAPI_URL";
    private final String host;

    @Inject
    private Client client;

    @Inject
    private AktorService aktorService;

    public VeilarbregisteringService() {
        Supplier<String> naisUrl = () -> UrlUtils.clusterUrlForApplication("veilarbregistrering") + "/veilarbregistrering/api";
        this.host = getOptionalProperty(VEILARBREGISTRERINGAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    public Try<BrukerRegistreringWrapper> hentRegistrering(String aktorId) {
        return Try.of(() -> {
            String fnr = aktorService.getFnr(aktorId).get();
            String url = String.format("%s/registrering?fnr=%s", host, fnr);
            return client.target(url).request().get();
        })
                .filter((resp) -> resp.getStatus() >= 200 && resp.getStatus() < 300)
                .map((resp) -> {
                    if (resp.getStatus() == 204){
                        return null;
                    }
                    return resp.readEntity(BrukerRegistreringWrapper.class);
                })
                .onFailure((error) -> log.warn("Fail to request registrering: " + error));
    }
}
