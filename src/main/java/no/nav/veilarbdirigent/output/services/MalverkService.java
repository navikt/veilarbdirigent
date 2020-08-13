package no.nav.veilarbdirigent.output.services;

import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.UrlUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.function.Supplier;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;

@Slf4j
public class MalverkService {
    public static final String VEILARBMALVERKAPI_URL_PROPERTY = "VEILARBMALVERKAPI_URL";
    private final String host;

    private OkHttpClient client;

    public MalverkService(OkHttpClient client) {
        this.client = client;
        Supplier<String> naisUrl = () -> UrlUtils.clusterUrlForApplication("veilarbmalverk") + "/veilarbmalverk/api";
        this.host = getOptionalProperty(VEILARBMALVERKAPI_URL_PROPERTY).orElseGet(naisUrl);
    }


    @SneakyThrows
    public Try<String> hentMal(String name) {
        String url = String.format("%s/mal/%s", host, name);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try{
            var resp = client.newCall(request).execute();
            return Try.success(resp.body().string());
        }
        catch (Exception e){
            log.warn("Fail request to malverk: " + name, e);
            return Try.failure(e);
        }
    }
}
