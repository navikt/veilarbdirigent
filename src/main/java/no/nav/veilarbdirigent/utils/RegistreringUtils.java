package no.nav.veilarbdirigent.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
@Slf4j
public class RegistreringUtils {

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
