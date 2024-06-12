package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.UrlUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;

@Slf4j
public class ArbeidssoekerregisterClient {

    private final String apiUrl;

    private final Supplier<String> machineToMachineTokenSupplier;

    private final OkHttpClient client;

    public ArbeidssoekerregisterClient(String apiUrl, Supplier<String> machineToMachineTokenSupplier) {
        this.apiUrl = apiUrl;
        this.machineToMachineTokenSupplier = machineToMachineTokenSupplier;
        this.client = RestClient.baseClient();
    }

    @SneakyThrows
    public List<ArbeidssoekerPeriodeResponse> hentArbeidsoekerPerioder(Fnr fnr) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/v1/veileder/arbeidssoekerperioder");
        var body = JsonUtils.toJson(new ArbeidssoekerperiodeRequest(fnr.get()));
        log.info("Hent arbeidssøkerperioder");

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + machineToMachineTokenSupplier.get())
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseArrayOrThrow(response, ArbeidssoekerPeriodeResponse.class);
        }
        catch (Exception e){
            log.error("Error hent arbeidssøkerperioder " + e);
            throw e;
        }
    }

    @SneakyThrows
    public List<ProfileringResponse> hentProfileringer(Fnr fnr, UUID arbeidssøkerperiodeId) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/v1/veileder/profilering");
        var body = JsonUtils.toJson(new ProfileringRequest(fnr.get(), arbeidssøkerperiodeId));
        log.info("Hent profileringer");

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + machineToMachineTokenSupplier.get())
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseArrayOrThrow(response, ProfileringResponse.class);
        }
        catch (Exception e){
            log.error("Error hent profileringer " + e);
            throw e;
        }
    }

    static class ArbeidssoekerPeriodeResponse {
        UUID periodeId;
        MetadataResponse startet;
        MetadataResponse avsluttet;
    }

    static class MetadataResponse {
        ZonedDateTime tidspunkt;
        UtførtAv utfoertAv;
        String kilde;
        String aarsak;
        TidspunktFraKildeResponse tidspunktFraKilde;
    }

    static class ProfileringResponse {
        String profileringId;
        String periodeId;
        ProfileringSendtInnAv profileringSendtInnAv;
        ProfileringsResultat profilertTil;
    }

    static class ProfileringSendtInnAv {
        LocalDateTime tidspunkt;
        UtførtAv utfoertAv;
    }

    static class UtførtAv {
        BrukerType type;
        String id;
    }

    enum BrukerType {UKJENT_VERDI, UDEFINERT, VEILEDER, SYSTEM, SLUTTBRUKER}

    record ArbeidssoekerperiodeRequest(String identitetsnummer) { }

    static class TidspunktFraKildeResponse {
        ZonedDateTime tidspunkt;
        AvviksTypeResponse avviksType;
    }

    enum AvviksTypeResponse {UKJENT_VERDI, FORSINKELSE, RETTING}

    enum ProfileringsResultat {UKJENT_VERDI, UDEFINERT, ANTATT_GODE_MULIGHETER, ANTATT_BEHOV_FOR_VEILEDNING, OPPGITT_HINDRINGER}

    record ProfileringRequest(String identitetsnummer, UUID periodeId) { }
}
