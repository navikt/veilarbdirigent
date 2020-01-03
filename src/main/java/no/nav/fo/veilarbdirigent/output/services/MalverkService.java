package no.nav.fo.veilarbdirigent.output.services;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.util.UrlUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Service
@Slf4j
public class MalverkService {
    public static final String VEILARBMALVERKAPI_URL_PROPERTY = "VEILARBMALVERKAPI_URL";
    private final String host;

    @Inject
    private Client client;

    public MalverkService() {
        String naisUrl = UrlUtils.clusterUrlForApplication("veilarbmalverk") + "/veilarbmalverk/api";
        this.host = getOptionalProperty(VEILARBMALVERKAPI_URL_PROPERTY).orElse(naisUrl);
    }

    public Try<String> hentMal(String name) {
        String url = String.format("%s/mal/%s", host, name);
        return Try.of(() -> client.target(url).request().get())
                .filter((resp) -> resp.getStatus() >= 200 && resp.getStatus() < 300)
                .map((resp) -> resp.readEntity(String.class))
                .onFailure((error) -> log.warn("Fail request to malverk: " + name, error));
    }
}
