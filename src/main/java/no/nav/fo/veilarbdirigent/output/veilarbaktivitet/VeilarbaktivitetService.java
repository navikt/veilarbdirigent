package no.nav.fo.veilarbdirigent.output.veilarbaktivitet;

import io.vavr.control.Try;
import no.nav.fo.veilarbaktivitet.domain.AktivitetDTO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Service
public class VeilarbaktivitetService {
    public static final String VEILARBAKTIVITETAPI_URL_PROPERTY = "VEILARBAKTIVITETAPI_URL";
    private final String host;

    @Inject
    private Client client;

    public VeilarbaktivitetService() {
        this.host = getRequiredProperty(VEILARBAKTIVITETAPI_URL_PROPERTY);
    }

    public static class VeilArbAktivitetServiceException extends Exception {
        VeilArbAktivitetServiceException(String msg) {
            super(msg);
        }
    }

    public Try<AktivitetDTO> lagAktivitet(String aktorId, AktivitetDTO data) {
        String url = String.format("%s/aktivitet/ny?aktorId=%s", host, aktorId);
        Invocation.Builder request = client.target(url).request();
        Response post = request.post(Entity.entity(data, MediaType.APPLICATION_JSON));

        if (post.getStatus() >= 200 && post.getStatus() < 300) {
            AktivitetDTO response = post.readEntity(AktivitetDTO.class);
            return Try.success(response);
        } else {
            String message = post.readEntity(String.class);
            return Try.failure(new VeilArbAktivitetServiceException(message));
        }
    }
}
