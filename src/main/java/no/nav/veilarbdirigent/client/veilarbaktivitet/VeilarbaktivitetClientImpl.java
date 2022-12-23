package no.nav.veilarbdirigent.client.veilarbaktivitet;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.utils.UrlUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.function.Supplier;

import static java.lang.String.format;
import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;
import static no.nav.common.rest.client.RestUtils.createBearerToken;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class VeilarbaktivitetClientImpl implements VeilarbaktivitetClient {

    private final String apiUrl;

    private final Supplier<String> serviceTokenSupplier;

    private final OkHttpClient client;

    public VeilarbaktivitetClientImpl(String apiUrl, Supplier<String> serviceTokenSupplier) {
        this.apiUrl = apiUrl;
        this.serviceTokenSupplier = serviceTokenSupplier;
        this.client = RestClient.baseClient();
    }

    @Override
    public Try<String> lagAktivitet(AktorId aktorId, String data) {
        String url = UrlUtils.joinPaths(apiUrl, format("/api/aktivitet/ny?aktorId=%s&automatisk=true", aktorId.get()));

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, data))
                .addHeader(AUTHORIZATION, createBearerToken(serviceTokenSupplier.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return Try.success(response.body().string());
            } else {
                log.error(String.format("Failed call lagAktivitet, http status %s", response.code()));
                return Try.failure(new RuntimeException(response.body().string()));
            }
        } catch (Exception e){
            return Try.failure(e);
        }
    }
}
