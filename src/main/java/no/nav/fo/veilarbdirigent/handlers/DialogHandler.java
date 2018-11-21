package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.fo.veilarbdialog.domain.NyHenvendelseDTO;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.api.*;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbdialogService;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class DialogHandler implements MessageHandler, Actuator<DialogHandler.OppfolgingData, NyHenvendelseDTO> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_DIALOG");

    @Inject
    Core core;
    @Inject
    private VeilarbdialogService service;

    private final List<String> sykmeldtBrukerTyper = List.of(
            "SKAL_TIL_NY_ARBEIDSGIVER",
            "SKAL_TIL_SAMME_ARBEIDSGIVER"
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

            boolean erNySykmeldtBrukerRegistrert = sykmeldtBrukerTyper.contains(msg.getSykmeldtBrukerType());

            if (erNySykmeldtBrukerRegistrert) {
                return List.of(
                        new Task<>()
                                .withId(String.valueOf(msg.getId()) + "dialog")
                                .withType(TYPE)
                                .withData(new TypedField<>(new OppfolgingData(msg))));
            }

            return List.empty();
        } else {
            return List.empty();
        }
    }


    @Override
    public Try<NyHenvendelseDTO> handle(DialogHandler.OppfolgingData data) {
        return service.lagDialog("{\n" +
                "  \"overskrift\": \"Hei!\",\n" +
                "  \"tekst\": \"Hei!\\nDu har svart at du trenger mer veiledning nå som retten til sykepenger nærmer seg slutten. Her kan du kommunisere med NAV-veilederen din. Du kan stille spørsmål eller informere om behovene dine.\"\n" +
                "}");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OppfolgingData {
        public OppfolgingDataFraFeed feedelement;
    }
}
