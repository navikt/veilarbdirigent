package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
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
    public SisteSamletInformasjon hentSisteSamletInformasjon(Fnr fnr) {
        String url = UrlUtils.joinPaths(apiUrl, "/api/v1/veileder/samlet-informasjon?siste=true");
        var body = JsonUtils.toJson(new SamletInformasjonRequest(fnr.get()));
        log.info("Hent arbeidssøkerperioder");

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + machineToMachineTokenSupplier.get())
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            var samletInformasjon = RestUtils.parseJsonResponseOrThrow(response, SamletInformasjon.class);
            if (samletInformasjon.profileringer.size() > 1) {
                throw new RuntimeException("Mottok fler enn en profilering - dette skal aldri skje");
            }
            if (samletInformasjon.arbeidssoekerperioder.size() > 1) {
                throw new RuntimeException("Mottok fler enn en arbeidssøkerperioder - dette skal aldri skje");
            }
            return new SisteSamletInformasjon(
                    samletInformasjon.arbeidssoekerperioder.stream().findFirst(),
                    samletInformasjon.profileringer.stream().findFirst()
            );
        } catch (Exception e) {
            log.error("Error hent samlet-informasjon " + e);
            throw e;
        }
    }

    public record SisteSamletInformasjon(Optional<ArbeidssoekerPeriode> arbeidssoekerperiode,
                                         Optional<Profilering> profilering) {
    }

    public static class SamletInformasjon {
        public List<ArbeidssoekerPeriode> arbeidssoekerperioder;
        @JsonProperty("profilering")
        public List<Profilering> profileringer;
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

    record SamletInformasjonRequest(String identitetsnummer) {
    }

    static class TidspunktFraKildeResponse {
        ZonedDateTime tidspunkt;
        AvviksTypeResponse avviksType;
    }

    enum AvviksTypeResponse {UKJENT_VERDI, FORSINKELSE, RETTING}

    public enum ProfileringsResultat {UKJENT_VERDI, UDEFINERT, ANTATT_GODE_MULIGHETER, ANTATT_BEHOV_FOR_VEILEDNING, OPPGITT_HINDRINGER}
}
