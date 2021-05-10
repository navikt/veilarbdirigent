package no.nav.veilarbdirigent.utils;

import io.vavr.collection.List;

public class RegistreringUtils {

    private final static List<String> sykmeldtBrukerTyper = List.of("SKAL_TIL_NY_ARBEIDSGIVER");

    private final static List<String> registeringForslag = List.of(
            "STANDARD_INNSATS",
            "SITUASJONSBESTEMT_INNSATS",
            "BEHOV_FOR_ARBEIDSEVNEVURDERING"
    );

    public static boolean erNyregistrert(String foreslattInnsatsgruppe) {
        return registeringForslag.contains(foreslattInnsatsgruppe);
    }

    public static boolean erNySykmeldtBrukerRegistrert(String sykmeldtBrukerType) {
        return sykmeldtBrukerTyper.contains(sykmeldtBrukerType);
    }

}
