package no.nav.veilarbdirigent.output.services;

import io.vavr.control.Try;
import no.nav.common.utils.UrlUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;
import static org.slf4j.LoggerFactory.getLogger;

public class VeilarbaktivitetService {
    private static final Logger LOG = getLogger(VeilarbaktivitetService.class);

    public static final String VEILARBAKTIVITETAPI_URL_PROPERTY = "VEILARBAKTIVITETAPI_URL";
    private final String host;
    public static final okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public VeilarbaktivitetService(OkHttpClient client) {
        this.client = client;
        Supplier<String> naisUrl =  () -> UrlUtils.clusterUrlForApplication("veilarbaktivitet") + "/veilarbaktivitet/api";
        this.host = getOptionalProperty(VEILARBAKTIVITETAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    public static class VeilArbAktivitetServiceException extends Exception {
        VeilArbAktivitetServiceException(String msg) {
            super(msg);
        }
    }

    public Try<String> lagAktivitet(String aktorId, String data) {
        String url = String.format("%s/aktivitet/ny?aktorId=%s&automatisk=true", host, aktorId);

        RequestBody body = RequestBody.create(JSON, data);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try{
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return Try.success(response.body().string());
            } else {
                return Try.failure(new VeilArbAktivitetServiceException(response.body().string()));
            }
        }
        catch (Exception e){
            return Try.failure(e);
        }
    }
}
