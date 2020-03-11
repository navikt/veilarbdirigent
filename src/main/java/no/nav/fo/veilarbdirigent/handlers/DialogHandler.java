package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.api.*;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.fo.veilarbdirigent.output.services.VeilarbdialogService;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class DialogHandler implements MessageHandler, Actuator<DialogHandler.OppfolgingData, String> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_DIALOG");

    @Inject
    Core core;
    @Inject
    private VeilarbdialogService service;

    private final List<String> sykmeldtBrukerTyper = List.of(
            "SKAL_TIL_NY_ARBEIDSGIVER",
            "SKAL_TIL_SAMME_ARBEIDSGIVER"
    );

    private static final String behovForArbeidsevnevurdering = "BEHOV_FOR_ARBEIDSEVNEVURDERING";
    private static final String sykemeldtDialog = "sykemeldt_dialog";
    private static final String arbeidsevnevurderingDialog = "arbeidsevnevurdering_dialog";


    private static final String sykemeldtJson = "{\n" +
            "\"overskrift\": \"Mer veiledning fra NAV\",\n" +
            "\"tekst\": \"Hei!\\n" +
            "Du har svart at du trenger mer veiledning nå som retten til sykepenger nærmer seg slutten. Vi vil veilede deg videre og trenger derfor å vite litt mer.\\n" +
            "Du kan velge om du vil fortelle om situasjonen din \\n" +
            "- i et møte med veilederen din på NAV-kontoret\\n" +
            "- i en telefonsamtale\\n" +
            "- her i dialogen\\n" +
            "Skriv svaret ditt i feltet over. Hvis du velger \\\"her i dialogen\\\", kan du fortelle mer allerede nå.\"\n" +
            "}";

    private static final String arbeidsevnevurderingJson = "{\n" +
            "\"overskrift\": \"Mer veiledning fra NAV\",\n" +
            "\"tekst\": \"Hei!\\n" +
            "Du har svart at du har utfordringer som hindrer deg i å søke eller være i jobb. Vi vil veilede deg videre og trenger derfor å vite litt mer.\\n" +
            "Du kan velge om du vil fortelle om situasjonen din \\n" +
            "- i et møte med veilederen din på NAV-kontoret\\n" +
            "- i en telefonsamtale\\n" +
            "- her i dialogen\\n" +
            "Skriv svaret ditt i feltet over. Hvis du velger \\\"her i dialogen\\\", kan du fortelle mer allerede nå.\"\n" +
            "}";

    private static final HashMap<String, String> meldinger = HashMap.ofEntries(
            Map.entry(sykemeldtDialog, sykemeldtJson),
            Map.entry(arbeidsevnevurderingDialog, arbeidsevnevurderingJson)
    );


    @PostConstruct
    public void register() {
        core.registerHandler(this);
        core.registerActuator(TYPE, this);
    }

    @Override
    public List<Task> handle(Message message) {
        if (message instanceof OppfolgingDataFraFeed) {
            OppfolgingDataFraFeed msg = (OppfolgingDataFraFeed) message;


            // creating a Calendar object
            Calendar c = Calendar.getInstance();
            c.set(2020, Calendar.MARCH, 12, 0, 0, 0);
            Date turnOfDate = c.getTime();

            if (Optional.ofNullable(msg.getOpprettet()).map(d -> d.after(turnOfDate)).orElse(true)){
                return List.empty();
            }

            boolean erNySykmeldtBrukerRegistrert = sykmeldtBrukerTyper.contains(msg.getSykmeldtBrukerType());

            if (erNySykmeldtBrukerRegistrert) {
                return List.of(
                        new Task<>()
                                .withId(String.valueOf(msg.getId()) + "sykemeldtDialog")
                                .withType(TYPE)
                                .withData(new TypedField<>(new OppfolgingData(msg, sykemeldtDialog))));
            } else if (behovForArbeidsevnevurdering.equals(msg.getForeslattInnsatsgruppe())) {
                return List.of(
                        new Task<>()
                                .withId(String.valueOf(msg.getId()) + "arbeidsevneDialog")
                                .withType(TYPE)
                                .withData(new TypedField<>(new OppfolgingData(msg, arbeidsevnevurderingDialog))));
            }

            return List.empty();
        } else {
            return List.empty();
        }
    }


    @Override
    public Try<String> handle(DialogHandler.OppfolgingData data) {
        String dialogJson = meldinger
                .get(data.meldingsName)
                .getOrElse(sykemeldtJson);

        return service.lagDialog(data.feedelement.getAktorId(), dialogJson);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OppfolgingData {
        public OppfolgingData(OppfolgingDataFraFeed feedelement) {
            this.feedelement = feedelement;
        }

        public OppfolgingDataFraFeed feedelement;
        public String meldingsName;
    }
}
