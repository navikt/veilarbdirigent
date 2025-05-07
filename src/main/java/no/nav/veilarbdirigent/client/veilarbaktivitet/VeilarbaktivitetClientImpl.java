package no.nav.veilarbdirigent.client.veilarbaktivitet;

import com.nimbusds.jwt.JWTClaimsSet;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.token_client.utils.TokenUtils;
import no.nav.common.utils.UrlUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    public Try<String> lagAktivitet(String data, UUID oppfolgingsPeriodeId) {
        String url = UrlUtils.joinPaths(apiUrl, format("/veilarbaktivitet/api/aktivitet/%s/ny?automatisk=true", oppfolgingsPeriodeId.toString()));

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, data))
                .addHeader(AUTHORIZATION, createBearerToken(serviceTokenSupplier.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return Try.success(response.body().string());
            } else {
                var message = Optional.ofNullable(response.body().string())
                    .filter(maybeMessage -> maybeMessage != null && !maybeMessage.isEmpty())
                    .orElse(String.format("Failed call lagAktivitet, http status %s", response.code()));
                return Try.failure(new RuntimeException(message));
            }
        } catch (Exception e){
            return Try.failure(e);
        }
    }

    static final String KAFKA_CONSUMER_FEATURE_TOGGLE_NAME = "veilarbdirigent.oppfolgingsperiode.consumer.disabled";

    @Override
    public Try<Boolean> isOppfolgingsperiodeConsumerDisabledToggle() {
        String url = UrlUtils.joinPaths(apiUrl, format("/veilarbaktivitet/api/feature?feature=%s", KAFKA_CONSUMER_FEATURE_TOGGLE_NAME));
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader(AUTHORIZATION, createBearerToken(serviceTokenSupplier.get()))
                .build();
        try(Response response = client.newCall(request).execute()) {
            Map<String, Boolean> map = JsonUtils.fromJson(response.body().string(), Map.class);
            var isDisabled = map.get(KAFKA_CONSUMER_FEATURE_TOGGLE_NAME);
            return Try.success(isDisabled);
        } catch (Exception e) {
            return Try.failure(e);
        }
    }
}
