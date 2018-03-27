package no.nav.fo.veilarbdirigent.service.aktivitet;

import io.vavr.control.Try;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.fo.veilarbaktivitet.domain.AktivitetDTO;
import no.nav.sbl.rest.RestUtils;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Service
public class VeilarbaktivitetService {
    public static final String VEILARBAKTIVITETAPI_URL_PROPERTY = "VEILARBAKTIVITETAPI_URL";
    private final String host;

    public VeilarbaktivitetService() {
        this.host = getRequiredProperty(VEILARBAKTIVITETAPI_URL_PROPERTY);
    }

    public Client client() {
        Client client = RestUtils.createClient();
        client.register(new SystemUserOidcTokenProviderFilter());
        return client;
    }

    private static class SystemUserOidcTokenProviderFilter implements ClientRequestFilter {
        private SystemUserTokenProvider systemUserTokenProvider = new SystemUserTokenProvider();

        @Override
        public void filter(ClientRequestContext clientRequestContext) throws IOException {
            clientRequestContext.getHeaders().putSingle("Authorization", "Bearer " + systemUserTokenProvider.getToken());
        }
    }

    public static class VeilArbAktivitetServiceException extends Exception {
        VeilArbAktivitetServiceException(String msg) {
            super(msg);
        }
    }

    public Try<AktivitetDTO> lagAktivitet(String aktorId, AktivitetDTO data) {
        String url = String.format("%s/aktivitet/ny?aktorId=%s", host, aktorId);
        Invocation.Builder request = client().target(url).request();
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
