package no.nav.veilarbdirigent.client.arbeidssoekerregisteret;

import com.google.common.collect.Lists;
import no.nav.common.rest.client.RestClient;
import okhttp3.OkHttpClient;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ArbeidssoekerregisterClient {

    private final String apiUrl;

    private final Supplier<String> machineToMachineTokenSupplier;

    private final OkHttpClient client;

    public ArbeidssoekerregisterClient(String apiUrl, Supplier<String> machineToMachineTokenSupplier) {
        this.apiUrl = apiUrl;
        this.machineToMachineTokenSupplier = machineToMachineTokenSupplier;
        this.client = RestClient.baseClient();
    }

    public List<ArbeidssoekerPeriodeResponse> hentArbeidsoekerPerioder(ArbeidssoekerperiodeRequest request) {
        return Lists.newArrayList();
    }

    public ProfileringsResultat hentProfilering(ProfileringRequest request) {
        return null;
    }

    static class ArbeidssoekerPeriodeResponse {
        UUID periodeId;
        MetadataResponse startet;
        MetadataResponse avsluttet;
    }

    static class MetadataResponse {
        ZonedDateTime tidspunkt;
        BrukerResponse utfoertAv;
        String kilde;
        String aarsak;
        TispunktFraKildeResponse tidspunktFraKilde;

    }

    static class BrukerResponse {
        BrukerType type;
        String id;
    }
    enum BrukerType {UKJENT_VERDI, UDEFINERT, VEILEDER, SYSTEM, SLUTTBRUKER}
    record ArbeidssoekerperiodeRequest(String identitetsnummer) {
    }
    static class TispunktFraKildeResponse {
        ZonedDateTime tidspunkt;
        AvviksTypeResponse avviksType;
    }
    enum AvviksTypeResponse {UKJENT_VERDI, FORSINKELSE, RETTING}
    enum ProfileringsResultat {UKJENT_VERDI, UDEFINERT, ANTATT_GODE_MULIGHETER, ANTATT_BEHOV_FOR_VEILEDNING, OPPGITT_HINDRINGER}

    record ProfileringRequest(String identitetsnummer, UUID periodeId) {
    }
}
