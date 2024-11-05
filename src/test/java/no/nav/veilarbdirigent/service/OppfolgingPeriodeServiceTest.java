package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.repository.TaskRepository;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient.ProfileringsResultat.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OppfolgingPeriodeServiceTest {

    private final AktorOppslagClient aktorOppslagClient = Mockito.mock(AktorOppslagClient.class);
    private final VeilarboppfolgingClient veilarboppfolgingClient = Mockito.mock(VeilarboppfolgingClient.class);
    private final ArbeidssoekerregisterClient arbeidssoekerregisterClient = Mockito.mock(ArbeidssoekerregisterClient.class);
    private final TaskProcessorService taskProcessorService = Mockito.mock(TaskProcessorService.class);
    private final TaskRepository taskRepository = Mockito.mock(TaskRepository.class);

    OppfolgingPeriodeService oppfolgingPeriodeService = new OppfolgingPeriodeService(aktorOppslagClient,
            veilarboppfolgingClient,
            arbeidssoekerregisterClient,
            taskProcessorService,
            taskRepository);

    @Test
    public void skalLageCVKortForArbeidssøker() {
        when(arbeidssoekerregisterClient.hentSisteSamletInformasjon(any())).thenReturn(sisteSamletInformasjon());
        when(veilarboppfolgingClient.hentOppfolgingsperioder(any())).thenReturn(List.of(oppfølgingsperiode()));
        when(taskProcessorService.processOpprettAktivitetTask(any())).thenReturn(jobbprofilAktivitetTask());
        var oppfolgingsperiode = OppfolgingsperiodeDto.builder()
                .aktorId("123")
                .startDato(ZonedDateTime.now().minusMinutes(60))
                .startetBegrunnelse(OppfolgingsperiodeDto.StartetBegrunnelseDTO.ARBEIDSSOKER).build();

        oppfolgingPeriodeService.behandleKafkaMeldingLogikk(oppfolgingsperiode);

        verify(taskRepository, times(1)).insert(any());
    }

    @Test
    public void skalIkkeLageCvKortNårProfileringErUkjentVerdi() {
        var profileringUkjentVerdi = profilering(ZonedDateTime.now().minusMinutes(1), UKJENT_VERDI);
        var samletInformasjon = new ArbeidssoekerregisterClient.SisteSamletInformasjon(
                Optional.of(arbeidssoekerPeriode()),
                Optional.of(profileringUkjentVerdi)
        );
        when(arbeidssoekerregisterClient.hentSisteSamletInformasjon(any())).thenReturn(samletInformasjon);
        when(veilarboppfolgingClient.hentOppfolgingsperioder(any())).thenReturn(List.of(oppfølgingsperiode()));
        when(taskProcessorService.processOpprettAktivitetTask(any())).thenReturn(jobbprofilAktivitetTask());
        var oppfolgingsperiode = OppfolgingsperiodeDto.builder()
                .aktorId("123")
                .startDato(ZonedDateTime.now().minusMinutes(60))
                .startetBegrunnelse(OppfolgingsperiodeDto.StartetBegrunnelseDTO.ARBEIDSSOKER).build();

        oppfolgingPeriodeService.behandleKafkaMeldingLogikk(oppfolgingsperiode);

        verify(taskRepository, never()).insert(any());
    }

    @Test
    public void skalIkkeLageCvKortNårProfileringMangler() {
        var samletInformasjon = new ArbeidssoekerregisterClient.SisteSamletInformasjon(
                Optional.of(arbeidssoekerPeriode()),
                Optional.empty()
        );
        when(arbeidssoekerregisterClient.hentSisteSamletInformasjon(any())).thenReturn(samletInformasjon);
        when(veilarboppfolgingClient.hentOppfolgingsperioder(any())).thenReturn(List.of(oppfølgingsperiode()));
        when(taskProcessorService.processOpprettAktivitetTask(any())).thenReturn(jobbprofilAktivitetTask());
        var oppfolgingsperiode = OppfolgingsperiodeDto.builder()
                .aktorId("123")
                .startDato(ZonedDateTime.now().minusMinutes(60))
                .startetBegrunnelse(OppfolgingsperiodeDto.StartetBegrunnelseDTO.ARBEIDSSOKER).build();

        oppfolgingPeriodeService.behandleKafkaMeldingLogikk(oppfolgingsperiode);

        verify(taskRepository, never()).insert(any());
    }

    @Test
    public void skalIkkeLageCvKortNårArbeidssøkerperiodeMangler() {
        var samletInformasjon = new ArbeidssoekerregisterClient.SisteSamletInformasjon(
                Optional.empty(),
                Optional.of(profilering())
        );
        when(arbeidssoekerregisterClient.hentSisteSamletInformasjon(any())).thenReturn(samletInformasjon);
        when(veilarboppfolgingClient.hentOppfolgingsperioder(any())).thenReturn(List.of(oppfølgingsperiode()));
        when(taskProcessorService.processOpprettAktivitetTask(any())).thenReturn(jobbprofilAktivitetTask());
        var oppfolgingsperiode = OppfolgingsperiodeDto.builder()
                .aktorId("123")
                .startDato(ZonedDateTime.now().minusMinutes(60))
                .startetBegrunnelse(OppfolgingsperiodeDto.StartetBegrunnelseDTO.ARBEIDSSOKER).build();

        oppfolgingPeriodeService.behandleKafkaMeldingLogikk(oppfolgingsperiode);

        verify(taskRepository, never()).insert(any());
    }

    private ArbeidssoekerregisterClient.SisteSamletInformasjon sisteSamletInformasjon() {
        return new ArbeidssoekerregisterClient.SisteSamletInformasjon(
                Optional.of(arbeidssoekerPeriode()),
                Optional.of(profilering())
        );
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

    private ArbeidssoekerregisterClient.Profilering profilering(ZonedDateTime tidspunkt, ArbeidssoekerregisterClient.ProfileringsResultat profilertTil) {
        ArbeidssoekerregisterClient.ProfileringSendtInnAv profileringSendtInnAv = new ArbeidssoekerregisterClient.ProfileringSendtInnAv();
        profileringSendtInnAv.tidspunkt = tidspunkt;
        ArbeidssoekerregisterClient.Profilering profilering = new ArbeidssoekerregisterClient.Profilering();
        profilering.profileringSendtInnAv = tidspunkt != null ? profileringSendtInnAv : null;
        profilering.profilertTil = profilertTil;
        return profilering;
    }

    private Try<String> jobbprofilAktivitetTask() {
        return Try.success("");
    }
}