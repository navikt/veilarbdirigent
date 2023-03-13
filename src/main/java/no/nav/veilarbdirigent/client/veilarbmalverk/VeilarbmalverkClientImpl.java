package no.nav.veilarbdirigent.client.veilarbmalverk;

import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static no.nav.common.utils.UrlUtils.joinPaths;

@Slf4j
public class VeilarbmalverkClientImpl implements VeilarbmalverkClient {

    private final String apiUrl;

    private final OkHttpClient client;

    public VeilarbmalverkClientImpl(String apiUrl) {
        this.apiUrl = apiUrl;
        this.client = RestClient.baseClient();
    }

    @Override
    @SneakyThrows
    public Try<String> hentMal(String name) {
        String url = joinPaths(apiUrl, "/api/mal", name);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return Try.success(response.body().string());
        } catch (Exception e){
            log.warn("Fail request to malverk: " + name, e);
            return Try.failure(e);
        }
    }
}
