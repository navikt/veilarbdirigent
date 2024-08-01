package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.*;
import no.nav.veilarbdirigent.repository.TaskRepository;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
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

    private final VeilarbregistreringClient veilarbregistreringClient = Mockito.mock(VeilarbregistreringClient.class);

    private final ArbeidssoekerregisterClient arbeidssoekerregisterClient = Mockito.mock(ArbeidssoekerregisterClient.class);

    private final TaskProcessorService taskProcessorService = Mockito.mock(TaskProcessorService.class);

    private final TaskRepository taskRepository = Mockito.mock(TaskRepository.class);

    OppfolgingPeriodeService oppfolgingPeriodeService = new OppfolgingPeriodeService(aktorOppslagClient,
            veilarboppfolgingClient,
            veilarbregistreringClient,
            arbeidssoekerregisterClient,
            taskProcessorService,
            taskRepository);

    @Test
    public void skalLageCVKortForArbeidssøker() {
        when(arbeidssoekerregisterClient.hentSamletInformasjon(any())).thenReturn(samletInformasjon());
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
    public void skalIkkeLageCvKortNårSisteProfileringErUkjentVerdi() {
        var samletInformasjon = samletInformasjon();
        var profileringGodeMuligheter = profilering(ZonedDateTime.now().minusDays(2), ANTATT_GODE_MULIGHETER);
        var profileringOppgitteHindringer = profilering(ZonedDateTime.now().minusDays(1), OPPGITT_HINDRINGER);
        var profileringUkjentVerdi = profilering(ZonedDateTime.now().minusMinutes(1), UKJENT_VERDI);
        samletInformasjon.profileringer = List.of(
                profileringOppgitteHindringer,
                profileringUkjentVerdi,
                profileringGodeMuligheter
        );
        when(arbeidssoekerregisterClient.hentSamletInformasjon(any())).thenReturn(samletInformasjon);
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
    public void skalLageCVKortNårBrukerHarKunEnProfileringMenDenErUtenTidspunkt() {
        var samletInformasjon = samletInformasjon();
        var profileringUtenTidspunkt = profilering(null, ANTATT_GODE_MULIGHETER);
        samletInformasjon.profileringer = List.of(profileringUtenTidspunkt);
        when(arbeidssoekerregisterClient.hentSamletInformasjon(any())).thenReturn(samletInformasjon);
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
    public void skalForkasteProfileringUtenTidspunktOgBrukeDenSomHarTidspunkt() {
        var samletInformasjon = samletInformasjon();
        var profileringUtenTidspunkt = profilering(ZonedDateTime.now().minusDays(1), ANTATT_GODE_MULIGHETER);
        var profileringMedTidspunkt = profilering(null, ANTATT_GODE_MULIGHETER);
        samletInformasjon.profileringer = List.of(profileringMedTidspunkt, profileringUtenTidspunkt);
        when(arbeidssoekerregisterClient.hentSamletInformasjon(any())).thenReturn(samletInformasjon);
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
    public void skalIkkeOppretteCVKortNårAlleProfileringerManglerTidspunkt() {
        var samletInformasjon = samletInformasjon();
        var profileringUtenTidspunkt1 = profilering(null, ANTATT_GODE_MULIGHETER);
        var profileringUtenTidspunkt2 = profilering(null, ANTATT_GODE_MULIGHETER);
        samletInformasjon.profileringer = List.of(profileringUtenTidspunkt1, profileringUtenTidspunkt2);
        when(arbeidssoekerregisterClient.hentSamletInformasjon(any())).thenReturn(samletInformasjon);
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
    public void skalLageCVKortForSykmeldt() {
        when(arbeidssoekerregisterClient.hentArbeidsoekerPerioder(any())).thenReturn(List.of());
        when(veilarboppfolgingClient.hentOppfolgingsperioder(any())).thenReturn(List.of(oppfølgingsperiode()));
        when(arbeidssoekerregisterClient.hentProfileringer(any(), any())).thenReturn(List.of(profilering()));
        when(taskProcessorService.processOpprettAktivitetTask(any())).thenReturn(jobbprofilAktivitetTask());
        when(veilarbregistreringClient.hentRegistrering(any())).thenReturn(Try.success(Optional.of(brukerRegistrering())));
        var oppfolgingsperiode = OppfolgingsperiodeDto.builder()
                .aktorId("123")
                .startDato(ZonedDateTime.now().minusMinutes(60))
                .startetBegrunnelse(OppfolgingsperiodeDto.StartetBegrunnelseDTO.SYKEMELDT_MER_OPPFOLGING).build();

        oppfolgingPeriodeService.behandleKafkaMeldingLogikk(oppfolgingsperiode);

        verify(taskRepository, times(1)).insert(any());
    }

    private ArbeidssoekerregisterClient.SamletInformasjon samletInformasjon() {
        ArbeidssoekerregisterClient.SamletInformasjon samletInformasjon= new ArbeidssoekerregisterClient.SamletInformasjon();
        samletInformasjon.arbeidssoekerperioder = List.of(arbeidssoekerPeriode());
        samletInformasjon.profileringer = List.of(profilering());
        return samletInformasjon;
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

    private BrukerRegistreringWrapper brukerRegistrering() {
        return new BrukerRegistreringWrapper(
                BrukerRegistreringType.SYKMELDT,
                null,
                new SykmeldtBrukerRegistrering(
                        LocalDateTime.now().minusDays(1),
                        new Besvarelse(null, FremtidigSituasjonSvar.NY_ARBEIDSGIVER)));
    }


    private Try<String> jobbprofilAktivitetTask() {
        return Try.success("");
    }
}