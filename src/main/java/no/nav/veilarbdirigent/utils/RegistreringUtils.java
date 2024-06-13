package no.nav.veilarbdirigent.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
@Slf4j
public class RegistreringUtils {

    private final static List<String> sykmeldtBrukerTyper = List.of("SKAL_TIL_NY_ARBEIDSGIVER");

    public static boolean erSykmeldtOgSkalIkkeTilbakeTilArbeidsgiver(String sykmeldtBrukerType) {
        if (sykmeldtBrukerType == null) {
            return false;
        }

        return sykmeldtBrukerTyper.contains(sykmeldtBrukerType);
    }

    public static LocalDateTime hentRegistreringDato(BrukerRegistreringWrapper brukerRegistrering) {
        OrdinaerBrukerRegistrering ordinaer = brukerRegistrering.getOrdinaerBrukerRegistrering();
        SykmeldtBrukerRegistrering sykmeldt = brukerRegistrering.getSykmeldtBrukerRegistrering();

        if (ordinaer != null) {
            return ordinaer.getOpprettetDato();
        }

        if (sykmeldt != null) {
            return sykmeldt.getOpprettetDato();
        }

        return null;
    }

    public static Besvarelse hentBesvarelse(BrukerRegistreringWrapper brukerRegistrering) {
        if (brukerRegistrering.getSykmeldtBrukerRegistrering() != null) {
            return brukerRegistrering.getSykmeldtBrukerRegistrering().getBesvarelse();
        } else {
            return brukerRegistrering.getOrdinaerBrukerRegistrering().getBesvarelse();
        }
    }

    public static boolean erSykmeldtOgSkalIkkeTilbakeTilArbeidsgiver(BrukerRegistreringWrapper brukerRegistrering) {
        if (brukerRegistrering == null || !BrukerRegistreringType.SYKMELDT.equals(brukerRegistrering.getType())) {
            return false;
        }

        FremtidigSituasjonSvar fremtidigSituasjon = brukerRegistrering.getSykmeldtBrukerRegistrering()
                .getBesvarelse()
                .getFremtidigSituasjon();

        return fremtidigSituasjon == FremtidigSituasjonSvar.USIKKER || fremtidigSituasjon == FremtidigSituasjonSvar.NY_ARBEIDSGIVER;
    }

    public static boolean erNyligRegistrert(LocalDateTime registreringsdato, List<Oppfolgingsperiode> oppfolgingsperioder) {
        // Hvis bruker kun har 1 oppfølgingsperiode og den er gjeldende så må registreringen være utført i denn perioden
        if (oppfolgingsperioder.size() == 1 && oppfolgingsperioder.get(0).getSluttDato() == null) {
            return true;
        }

        Oppfolgingsperiode forrigeOppfolgingsperiode = oppfolgingsperioder.stream()
                .filter(op -> op.getSluttDato() != null)
                .max(Comparator.comparing(Oppfolgingsperiode::getSluttDato))
                .orElseThrow(() -> new IllegalStateException("Fant ikke forrige oppfølgingsperiode for bruker"));

        /*
         Registreringen vil være utført i omtrent samme tid som starten på oppfølgingsperioden.
         Siden vi ikke har helt kontroll på at registreringen utføres etter at perioden har startet, så sjekker vi heller at den
         ble utført etter avslutningen på forrige periode.
        */
        boolean registreringsdatoAfter = registreringsdato.isAfter(forrigeOppfolgingsperiode.getSluttDato().toLocalDateTime());
        if (!registreringsdatoAfter) {
            log.info("Registreringsdato: {} ikke etter siste oppfølgingsperiode sluttdato: Forrige Oppfølgingsperiode: {} ", registreringsdato, forrigeOppfolgingsperiode);
        }
        return registreringsdatoAfter;
    }

}
