package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.fo.veilarbaktivitet.domain.AktivitetDTO;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.api.*;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.fo.veilarbdirigent.output.veilarbaktivitet.MalverkService;
import no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbaktivitetService;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class OppfolgingsHandler implements MessageHandler, Actuator<OppfolgingsHandler.OppfolgingData, AktivitetDTO> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

    @Inject
    Core core;
    @Inject
    private VeilarbaktivitetService service;
    @Inject
    private MalverkService malverk;

    private final String IVURD = "IVURD";
    private final String BKART = "BKART";

    @PostConstruct
    public void register() {
        core.registerHandler(this);
        core.registerActuator(TYPE, this);
    }


    @Override
    public List<Task> handle(Message message) {
        if (message instanceof OppfolgingDataFraFeed) {
            OppfolgingDataFraFeed msg = (OppfolgingDataFraFeed) message;

            boolean erNyRegistrert = IVURD.equals(msg.getInnsatsgruppe()) || BKART.equals(msg.getInnsatsgruppe());
            if (!erNyRegistrert) {
                return List.empty();
            }

            return List.of(
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "cv_aktivitet")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "cv_aktivitet"))),
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "jobbonsker")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "jobbonsker_aktivitet"))),
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "jobbsokerkompetanse")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "jobbsokerkompetanse_aktivitet"))),
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "soke_jobber")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "mulighet_i_arbeidsmarkedet_aktivitet")))
            );
        } else {
            return List.empty();
        }
    }

    @Override
    public Try<AktivitetDTO> handle(OppfolgingsHandler.OppfolgingData data) {
        return malverk.hentMal(data.predefineddataName)
                .flatMap((template) -> service.lagAktivitet(data.feedelement.getAktorId(), template));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OppfolgingData {
        public OppfolgingDataFraFeed feedelement;
        public String predefineddataName;
    }
}
