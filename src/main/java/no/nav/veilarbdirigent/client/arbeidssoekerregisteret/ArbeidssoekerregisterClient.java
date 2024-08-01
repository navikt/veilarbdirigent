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
    public List<ArbeidssoekerPeriode> hentArbeidsoekerPerioder(Fnr fnr) {
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
            return RestUtils.parseJsonResponseArrayOrThrow(response, ArbeidssoekerPeriode.class);
        }
        catch (Exception e){
            log.error("Error hent arbeidssøkerperioder " + e);
            throw e;
        }
    }

    @SneakyThrows
    public List<Profilering> hentProfileringer(Fnr fnr, UUID arbeidssøkerperiodeId) {
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
            return RestUtils.parseJsonResponseArrayOrThrow(response, Profilering.class);
        }
        catch (Exception e){
            log.error("Error hent profileringer " + e);
            throw e;
        }
    }

    @SneakyThrows
    public SamletInformasjon hentSamletInformasjon(Fnr fnr) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/v1/veileder/samlet-informasjon");
        var body = JsonUtils.toJson(new SamletInformasjonRequest(fnr.get()));
        log.info("Hent arbeidssøkerperioder");

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + machineToMachineTokenSupplier.get())
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, SamletInformasjon.class);
        }
        catch (Exception e){
            log.error("Error hent samlet-informasjon " + e);
            throw e;
        }
    }

    public static class SamletInformasjon {
        public List<ArbeidssoekerPeriode> arbeidssoekerperioder;
        public List<Profilering> profilering;
    }

    public static class ArbeidssoekerPeriode {
        public UUID periodeId;
        public Metadata startet;
        public Metadata avsluttet;
    }

    public static class Metadata {
        public ZonedDateTime tidspunkt;
        public UtførtAv utfoertAv;
        public String kilde;
        public String aarsak;
        public TidspunktFraKildeResponse tidspunktFraKilde;
    }

    public static class Profilering {
        public String profileringId;
        public String periodeId;
        public ProfileringSendtInnAv profileringSendtInnAv;
        public ProfileringsResultat profilertTil;
    }

    public static class ProfileringSendtInnAv {
        public ZonedDateTime tidspunkt;
        public UtførtAv utfoertAv;
    }

    static class UtførtAv {
        public BrukerType type;
        public String id;
    }

    enum BrukerType {UKJENT_VERDI, UDEFINERT, VEILEDER, SYSTEM, SLUTTBRUKER}

    record ArbeidssoekerperiodeRequest(String identitetsnummer) { }

    record SamletInformasjonRequest(String identitetsnummer) { }

    static class TidspunktFraKildeResponse {
        ZonedDateTime tidspunkt;
        AvviksTypeResponse avviksType;
    }

    enum AvviksTypeResponse {UKJENT_VERDI, FORSINKELSE, RETTING}

    public enum ProfileringsResultat {UKJENT_VERDI, UDEFINERT, ANTATT_GODE_MULIGHETER, ANTATT_BEHOV_FOR_VEILEDNING, OPPGITT_HINDRINGER}

    record ProfileringRequest(String identitetsnummer, UUID periodeId) { }
}
