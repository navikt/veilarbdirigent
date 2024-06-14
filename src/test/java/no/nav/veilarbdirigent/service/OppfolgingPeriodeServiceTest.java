package no.nav.veilarbdirigent.service;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.repository.TaskRepository;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient.ProfileringsResultat.ANTATT_BEHOV_FOR_VEILEDNING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OppfolgingPeriodeServiceTest {

    private final AktorOppslagClient aktorOppslagClient = Mockito.mock(AktorOppslagClient.class);

    private final VeilarboppfolgingClient veilarboppfolgingClient = Mockito.mock(VeilarboppfolgingClient.class);

    private final VeilarbregistreringClient veilarbregistreringClient = Mockito.mock(VeilarbregistreringClient.class);

    private final ArbeidssoekerregisterClient arbeidssoekerregisterClient = Mockito.mock(ArbeidssoekerregisterClient.class);

    private final TaskProcessorService taskProcessorService = Mockito.mock(TaskProcessorService.class);

    private final TaskRepository taskRepository = Mockito.mock(TaskRepository.class);

    private final JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);

    OppfolgingPeriodeService oppfolgingPeriodeService = new OppfolgingPeriodeService(aktorOppslagClient,
            veilarboppfolgingClient,
            veilarbregistreringClient,
            arbeidssoekerregisterClient,
            taskProcessorService,
            taskRepository,
            jdbcTemplate);

    @Test
    public void skalLageCVKortForArbeidssøker() {
        var sisteOppfølgingsperiode = SisteOppfolgingsperiodeV1.builder().aktorId("123").startDato(ZonedDateTime.now().minusMinutes(60)).build();

        when(arbeidssoekerregisterClient.hentArbeidsoekerPerioder(any())).thenReturn(List.of(arbeidssoekerPeriode()));
        when(veilarboppfolgingClient.hentOppfolgingsperioder(any())).thenReturn(List.of(oppfølgingsperiode()));
        when(arbeidssoekerregisterClient.hentProfileringer(any(), any())).thenReturn(List.of(profilering()));
        oppfolgingPeriodeService.behandleKafkaMeldingLogikk(sisteOppfølgingsperiode);
    }

    private ArbeidssoekerregisterClient.ArbeidssoekerPeriode arbeidssoekerPeriode() {
        ArbeidssoekerregisterClient.Metadata startetMetadata = new ArbeidssoekerregisterClient.Metadata();
        startetMetadata.tidspunkt = ZonedDateTime.now().minusMinutes(10);
        ArbeidssoekerregisterClient.ArbeidssoekerPeriode arbeidssoekerPeriode = new ArbeidssoekerregisterClient.ArbeidssoekerPeriode();
        arbeidssoekerPeriode.periodeId = UUID.randomUUID();
        arbeidssoekerPeriode.startet = startetMetadata;
        return arbeidssoekerPeriode;
    }

    private Oppfolgingsperiode oppfølgingsperiode() {
        return new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("123"), ZonedDateTime.now().minusDays(2), null);
    }

    private ArbeidssoekerregisterClient.Profilering profilering() {
        ArbeidssoekerregisterClient.ProfileringSendtInnAv profileringSendtInnAv = new ArbeidssoekerregisterClient.ProfileringSendtInnAv();
        profileringSendtInnAv.tidspunkt = ZonedDateTime.now().minusMinutes(10);
        ArbeidssoekerregisterClient.Profilering profilering = new ArbeidssoekerregisterClient.Profilering();
        profilering.profileringSendtInnAv = profileringSendtInnAv;
        profilering.profilertTil = ANTATT_BEHOV_FOR_VEILEDNING;
        return profilering;
    }
}