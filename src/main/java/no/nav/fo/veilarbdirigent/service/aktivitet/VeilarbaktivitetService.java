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

@Service
public class VeilarbaktivitetService {
    @Inject
    private Client client;

    public Either<String, AktivitetDTO> lagAktivitet(AktivitetDTO data) {
        Invocation.Builder request = client.target("").request();
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
