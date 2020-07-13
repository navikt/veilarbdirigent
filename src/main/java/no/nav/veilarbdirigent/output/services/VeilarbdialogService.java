package no.nav.veilarbdirigent.output.services;

import io.vavr.control.Try;
import no.nav.common.utils.UrlUtils;
import okhttp3.*;

import java.util.function.Supplier;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;

public class VeilarbdialogService {
    public static final String VEILARBDIALOGAPI_URL_PROPERTY = "VEILARBDIALOGAPI_URL";
    private final String host;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    private final OkHttpClient client;

    public VeilarbdialogService(OkHttpClient client) {
        this.client = client;
        Supplier<String> naisUrl = () -> UrlUtils.clusterUrlForApplication("veilarbdialog") + "/veilarbdialog/api";
        this.host = getOptionalProperty(VEILARBDIALOGAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    public static class VeilArbDialogServiceException extends Exception {
        VeilArbDialogServiceException(String msg) {
            super(msg);
        }
    }

    public Try<String> lagDialog(String aktorId, String data) {
        String url = String.format("%s/dialog?aktorId=%s", host, aktorId);

        RequestBody body = RequestBody.create(JSON, data);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                return Try.success(response.body().string());
            } else {
                return Try.failure(new VeilArbDialogServiceException(response.body().string()));
            }
        } catch (Exception e) {
            return Try.failure(e);
        }

    }
}
