package no.nav.fo.veilarbdirigent.service.aktivitet;

import io.vavr.control.Either;
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

    public VeilarbaktivitetService() {
        this.host = getRequiredProperty(VEILARBAKTIVITETAPI_URL_PROPERTY);
    }

    @Inject
    private Client client;

    public Either<String, AktivitetDTO> lagAktivitet(String aktorId, AktivitetDTO data) {
        String url = String.format("%s/aktivitet/ny?aktorId=%s", host, aktorId);
        Invocation.Builder request = client.target(url).request();
        Response post = request.post(Entity.entity(data, MediaType.APPLICATION_JSON));

        if (post.getStatus() >= 200 && post.getStatus() < 300) {
            AktivitetDTO response = post.readEntity(AktivitetDTO.class);
            return Either.right(response);
        } else {
            String message = post.readEntity(String.class);
            return Either.left(message);
        }
    }
}
