package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.Besvarelse;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.DinSituasjonSvar;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogService {

    private static final String permitertJson = "{\n" +
            "\"overskrift\": \"Permittering – automatisk melding fra NAV\",\n" +
            "\"tekst\": \"Hei!\\n" +
            "Ha tett kontakt med arbeidsgiveren din om situasjonen fremover, nå når du er permittert. Når du har begynt i jobben din igjen, eller mister jobben, så [gir du beskjed til NAV slik](https://www.nav.no/arbeid/no/dagpenger/#gi-beskjed-hvis-situasjonen-din-endrer-seg).\\n" +
            "Du finner informasjon om [dagpenger og permittering her](https://www.nav.no/arbeid/no/permittert).\\n" +
            "Hilsen NAV\"\n" +
            "}";

    private final AktorOppslagClient aktorOppslagClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final VeilarbdialogClient veilarbdialogClient;

    public Try<String> opprettDialogHvisBrukerErPermittert(AktorId aktorId) {
        Fnr brukerFnr = aktorOppslagClient.hentFnr(aktorId);

        Try<Optional<BrukerRegistreringWrapper>> maybeRegistreringData = veilarbregistreringClient.hentRegistrering(brukerFnr);

        if (maybeRegistreringData.isFailure()){
            return Try.failure(maybeRegistreringData.getCause());
        }

        if (maybeRegistreringData.get().isEmpty()) {
            log.info("Bruker med aktorId={} er ikke registrert i arbeidssokerregistrering og vil ikke få dialog om permittering", aktorId);
            return Try.success("Bruker ikke registrert gjennom arbeidssokerregistrering");
        }

        Optional<DinSituasjonSvar> svar = maybeRegistreringData.get()
                .map(RegistreringUtils::hentBesvarelse)
                .map(Besvarelse::getDinSituasjon);

        if (svar.map(DinSituasjonSvar.ER_PERMITTERT::equals).orElse(false)) {
            log.info("Oppretter dialog om permittering for bruker aktorId={}", aktorId);
            return veilarbdialogClient.lagDialog(aktorId, permitertJson);
        }

        return Try.success("Nothing to do here");
    }

}
