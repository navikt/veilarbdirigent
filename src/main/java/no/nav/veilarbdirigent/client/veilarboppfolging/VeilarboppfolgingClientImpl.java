package no.nav.veilarbdirigent.client.veilarboppfolging;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;
import java.util.function.Supplier;

import static no.nav.common.rest.client.RestUtils.createBearerToken;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class VeilarboppfolgingClientImpl implements VeilarboppfolgingClient {

    private final String apiUrl;

    private final Supplier<String> serviceTokenSupplier;

    private final OkHttpClient client;

    public VeilarboppfolgingClientImpl(String apiUrl, Supplier<String> serviceTokenSupplier) {
        this.apiUrl = apiUrl;
        this.serviceTokenSupplier = serviceTokenSupplier;
        this.client = RestClient.baseClient();
    }

    @SneakyThrows
    @Override
    public List<Oppfolgingsperiode> hentOppfolgingsperioder(Fnr fnr) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/oppfolging/oppfolgingsperioder?fnr=" + fnr);

        Request request = new Request.Builder()
                .url(url)
                .addHeader(AUTHORIZATION, createBearerToken(serviceTokenSupplier.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseArrayOrThrow(response, Oppfolgingsperiode.class);
        }
    }

}
