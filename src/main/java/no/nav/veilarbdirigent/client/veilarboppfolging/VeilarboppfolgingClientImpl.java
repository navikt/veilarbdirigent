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

@Slf4j
public class VeilarboppfolgingClientImpl implements VeilarboppfolgingClient {

    private final String apiUrl;

    private final Supplier<String> machineToMachineTokenSupplier;

    private final OkHttpClient client;

    public VeilarboppfolgingClientImpl(String apiUrl, Supplier<String> tokenClient) {
        this.apiUrl = apiUrl;
        this.machineToMachineTokenSupplier = tokenClient;
        this.client = RestClient.baseClient();
    }

    @SneakyThrows
    @Override
    public List<Oppfolgingsperiode> hentOppfolgingsperioder(Fnr fnr) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/oppfolging/oppfolgingsperioder?fnr=" + fnr);

        log.info("Hent oppfolgingsperioder");
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + machineToMachineTokenSupplier.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseArrayOrThrow(response, Oppfolgingsperiode.class);
        }
        catch (Exception e){
            log.error("Error hent oppfolgingsperiode " + e);
            throw e;
        }
    }

}
