package no.nav.veilarbdirigent.utils;

import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegistreringUtilsTest {

    @Test
    public void erNyligRegistrert__skal_returnere_true_hvis_bruker_kun_har_1_gjeldende_periode() {
        List<Oppfolgingsperiode> oppfolgingsperioder = List.of(
                new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("test"), ZonedDateTime.now(), null)
        );

        LocalDateTime registreringsdato = LocalDateTime.now();

        assertTrue(RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder));
    }

    @Test
    public void erNyligRegistrert__skal_returnere_true_hvis_registrering_er_etter_forrige_oppfolgingsperiode() {
        ZonedDateTime tidligerePeriodeStart = ZonedDateTime.now().minusMinutes(100);
        ZonedDateTime tidligerePeriodeSlutt = ZonedDateTime.now().minusMinutes(50);

        ZonedDateTime forrigePeriodeStart = ZonedDateTime.now().minusSeconds(100);
        ZonedDateTime forrigePeriodeSlutt = ZonedDateTime.now().minusSeconds(50);

        List<Oppfolgingsperiode> oppfolgingsperioder = List.of(
                new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("test"), tidligerePeriodeStart, tidligerePeriodeSlutt),
                new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("test"), forrigePeriodeStart, forrigePeriodeSlutt),
                new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("test"), ZonedDateTime.now(), null)
        );

        LocalDateTime registreringsdato = forrigePeriodeSlutt.plusSeconds(5).toLocalDateTime();

        assertTrue(RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder));
    }

    @Test
    public void erNyligRegistrert__skal_returnere_false_hvis_registrering_er_inne_i_forrige_oppfolgingsperiode() {
        ZonedDateTime tidligerePeriodeStart = ZonedDateTime.now().minusMinutes(100);
        ZonedDateTime tidligerePeriodeSlutt = ZonedDateTime.now().minusMinutes(50);

        ZonedDateTime forrigePeriodeStart = ZonedDateTime.now().minusSeconds(100);
        ZonedDateTime forrigePeriodeSlutt = ZonedDateTime.now().minusSeconds(50);

        List<Oppfolgingsperiode> oppfolgingsperioder = List.of(
                new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("test"), tidligerePeriodeStart, tidligerePeriodeSlutt),
                new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("test"), forrigePeriodeStart, forrigePeriodeSlutt),
                new Oppfolgingsperiode(UUID.randomUUID(), AktorId.of("test"), ZonedDateTime.now(), null)
        );

        LocalDateTime registreringsdato = forrigePeriodeStart.plusSeconds(5).toLocalDateTime();

        assertFalse(RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder));
    }

}
