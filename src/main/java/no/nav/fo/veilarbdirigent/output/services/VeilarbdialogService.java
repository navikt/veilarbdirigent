package no.nav.fo.veilarbdirigent.output.services;

import io.vavr.control.Try;
import no.nav.apiapp.util.UrlUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Service
public class VeilarbdialogService {
    public static final String VEILARBDIALOGAPI_URL_PROPERTY = "VEILARBDIALOGAPI_URL";
    private final String host;

    @Inject
    private Client client;

    public VeilarbdialogService() {
        Supplier<String> naisUrl = () -> UrlUtils.clusterUrlForApplication("veilarbdialog") + "/veilarbdialog/api";
        this.host = getOptionalProperty(VEILARBDIALOGAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    public static class VeilArbDialogServiceException extends Exception {
        VeilArbDialogServiceException(String msg) {
            super(msg);
        }
    }

    public Try<String> lagDialog(String aktorId, String data) {
        String url = String.format("%s/dialog?aktorId=%s", host, aktorId);
        Invocation.Builder request = client.target(url).request();
        Response post = request.post(Entity.entity(data, MediaType.APPLICATION_JSON));

        if (post.getStatus() >= 200 && post.getStatus() < 300) {
            String response = post.readEntity(String.class);
            return Try.success(response);
        } else {
            String message = post.readEntity(String.class);
            return Try.failure(new VeilArbDialogServiceException(message));
        }
    }
}