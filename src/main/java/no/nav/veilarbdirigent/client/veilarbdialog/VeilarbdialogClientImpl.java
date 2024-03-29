package no.nav.veilarbdirigent.client.veilarbdialog;

import io.vavr.control.Try;
import no.nav.common.rest.client.RestClient;
import no.nav.common.types.identer.AktorId;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Supplier;

import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;
import static no.nav.common.rest.client.RestUtils.createBearerToken;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class VeilarbdialogClientImpl implements VeilarbdialogClient {

    private final String apiUrl;

    private final Supplier<String> serviceTokenSupplier;

    private final OkHttpClient client;

    public VeilarbdialogClientImpl(String apiUrl, Supplier<String> serviceTokenSupplier) {
        this.apiUrl = apiUrl;
        this.serviceTokenSupplier = serviceTokenSupplier;
        this.client = RestClient.baseClient();
    }

    @Override
    public Try<String> lagDialog(AktorId aktorId, String data) {
        String url = joinPaths(apiUrl, "/api/dialog?aktorId=" + aktorId.get());

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, data))
                .addHeader(AUTHORIZATION, createBearerToken(serviceTokenSupplier.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return Try.success(response.body().string());
            } else if (response.code() == HttpStatus.CONTINUE.value()) {
                return Try.success("Dialog kan ikke opprettes fordi bruker kan ikke varsles");
            } else {
                var message = Optional.ofNullable(response.body().string())
                    .filter(maybeMessage -> maybeMessage != null && !maybeMessage.isEmpty())
                    .orElse(String.format("Failed call lagDialog, http status %s", response.code()));
                return Try.failure(new RuntimeException(message));
            }
        } catch (Exception e) {
            return Try.failure(e);
        }

    }
}
