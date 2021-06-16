package no.nav.veilarbdirigent.utils;

import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class RegistreringUtils {

    private final static List<String> sykmeldtBrukerTyper = List.of("SKAL_TIL_NY_ARBEIDSGIVER");

    private final static List<String> registeringForslag = List.of(
            "STANDARD_INNSATS",
            "SITUASJONSBESTEMT_INNSATS",
            "BEHOV_FOR_ARBEIDSEVNEVURDERING"
    );

    public static boolean erNyregistrert(String foreslattInnsatsgruppe) {
        if (foreslattInnsatsgruppe == null) {
            return false;
        }

        return registeringForslag.contains(foreslattInnsatsgruppe);
    }

    public static boolean erNySykmeldtBrukerRegistrert(String sykmeldtBrukerType) {
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

    public static boolean erNyregistrert(BrukerRegistreringWrapper brukerRegistrering) {
        if (brukerRegistrering == null || !BrukerRegistreringType.ORDINAER.equals(brukerRegistrering.getType())) {
            return false;
        }

        String innsatsgruppe = brukerRegistrering.getOrdinaerBrukerRegistrering()
                .getProfilering()
                .getInnsatsgruppe();

        return erNyregistrert(innsatsgruppe);
    }

    public static boolean erNySykmeldtBrukerRegistrert(BrukerRegistreringWrapper brukerRegistrering) {
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
        return registreringsdato.isAfter(forrigeOppfolgingsperiode.getSluttDato().toLocalDateTime());
    }

}
