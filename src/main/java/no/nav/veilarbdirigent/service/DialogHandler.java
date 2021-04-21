package no.nav.veilarbdirigent.service;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClientImpl;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClientImpl;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.Besvarelse;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.DinSituasjonSvar;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.OrdinaerBrukerRegistrering;
import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.core.api.*;
import no.nav.veilarbdirigent.old.input.OppfolgingDataFraFeed;

import javax.annotation.PostConstruct;

public class DialogHandler implements MessageHandler, Actuator<DialogHandler.OppfolgingData, String> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_DIALOG");

    private Core core;
    private VeilarbregistreringClientImpl veilarbregistreringClientImpl;
    private VeilarbdialogClientImpl service;

    private static final String permitertDialog = "kanskje_permitert_dialog";

    private static final String permitertJson = "{\n" +
            "\"overskrift\": \"Permittering – automatisk melding fra NAV\",\n" +
            "\"tekst\": \"Hei!\\n" +
            "Ha tett kontakt med arbeidsgiveren din om situasjonen fremover, nå når du er permittert. Når du har begynt i jobben din igjen, eller mister jobben, så [gir du beskjed til NAV slik](https://www.nav.no/arbeid/no/dagpenger/#gi-beskjed-hvis-situasjonen-din-endrer-seg).\\n" +
            "Du finner informasjon om [dagpenger og permittering her](https://www.nav.no/arbeid/no/permittert).\\n" +
            "Hilsen NAV\"\n" +
            "}";

    private final List<String> registeringForslag = List.of("STANDARD_INNSATS",
            "SITUASJONSBESTEMT_INNSATS",
            "BEHOV_FOR_ARBEIDSEVNEVURDERING");

    public DialogHandler(Core core, VeilarbregistreringClientImpl veilarbregistreringClientImpl, VeilarbdialogClientImpl service) {
        this.core = core;
        this.veilarbregistreringClientImpl = veilarbregistreringClientImpl;
        this.service = service;
    }


    @PostConstruct
    public void register() {
        core.registerHandler(this);
        core.registerActuator(TYPE, this);
    }

    @Override
    public List<Task> handle(Message message) {
        if (message instanceof OppfolgingDataFraFeed) {
            OppfolgingDataFraFeed msg = (OppfolgingDataFraFeed) message;

            boolean erNyRegistrert = registeringForslag.contains(msg.getForeslattInnsatsgruppe());
            if (erNyRegistrert) {
                return List.of(
                        new Task<>()
                                .withId(String.valueOf(msg.getId()) + "kanskjePermitert")
                                .withType(TYPE)
                                .withData(new TypedField<>(new OppfolgingData(msg, permitertDialog))));
            }
        }
        return List.empty();
    }


    @Override
    public Try<String> handle(DialogHandler.OppfolgingData data) {

        Try<BrukerRegistreringWrapper> registeringsData = veilarbregistreringClientImpl.hentRegistrering(data.feedelement.getAktorId());
        if(registeringsData.isFailure()){
            return Try.failure(registeringsData.getCause());
        }

        Option<DinSituasjonSvar> svar = registeringsData
                .toOption()
                .flatMap(Option::of)
                .map(BrukerRegistreringWrapper::getRegistrering)
                .map(OrdinaerBrukerRegistrering::getBesvarelse)
                .map(Besvarelse::getDinSituasjon);


        if(svar.map(DinSituasjonSvar.ER_PERMITTERT::equals).getOrElse(false)){
            return service.lagDialog(data.feedelement.getAktorId(), permitertJson);
        }

        return Try.success("Nothing to do here");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OppfolgingData {
        public OppfolgingDataFraFeed feedelement;
        public String meldingsName;
    }
}
