package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.fo.veilarbaktivitet.domain.AktivitetDTO;
import no.nav.fo.veilarbaktivitet.domain.AktivitetStatus;
import no.nav.fo.veilarbaktivitet.domain.AktivitetTypeDTO;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.PredefinedDataLoader;
import no.nav.fo.veilarbdirigent.core.api.*;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.fo.veilarbdirigent.service.aktivitet.VeilarbaktivitetService;
import no.nav.fo.veilarbdirigent.utils.Extrapolator;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static no.nav.fo.veilarbdirigent.utils.SerializerUtils.deserializeToDate;

public class OppfolgingsHandler implements MessageHandler, Actuator<OppfolgingsHandler.OppfolgingData, AktivitetDTO> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

    @Inject
    Core core;
    @Inject
    private VeilarbaktivitetService service;
    private Extrapolator extrapolator = new Extrapolator();

    @PostConstruct
    public void register() {
        core.registerHandler(this);
        core.registerActuator(TYPE, this);
    }


    @Override
    public List<Task> handle(Message message) {
        if (message instanceof OppfolgingDataFraFeed) {
            OppfolgingDataFraFeed msg = (OppfolgingDataFraFeed) message;
            return List.of(
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "jobbonsker")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "jobbonsker_aktivitet"))),
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "cv_aktivitet")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "cv_aktivitet"))),
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "jobbsokerkompetanse")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "jobbsokerkompetanse_aktivitet"))),
                    new Task<>()
                            .withId(String.valueOf(msg.getId()) + "soke_jobber")
                            .withType(TYPE)
                            .withData(new TypedField<>(new OppfolgingData(msg, "soke_jobber_aktivitet")))
            );
        } else {
            return List.empty();
        }
    }

    @Override
    public Either<String, Task<OppfolgingsHandler.OppfolgingData, AktivitetDTO>> handle(Task<OppfolgingsHandler.OppfolgingData, AktivitetDTO> task) {
        OppfolgingData data = task.getData().element;
        Option<AktivitetData> maybeAktivitetData = PredefinedDataLoader.get(data.predefineddataName, AktivitetData.class);

        if (maybeAktivitetData.isEmpty()) {
            throw new RuntimeException("Could not find predefined data " + data.predefineddataName);
        }

        AktivitetData predefinertData = maybeAktivitetData.get();

        return service.lagAktivitet(data.feedelement.getAktorId(), predefinertData.toDTO(extrapolator))
                .map((result) -> task.withResult(new TypedField<>(result)));
    }

    public static class AktivitetData {
        public AktivitetTypeDTO type;
        public String tittel;
        public String beskrivelse;
        public Long antallStillingerSokes;
        public String avtaleOppfolging;
        public String hensikt;
        public String oppfolging;
        public String lenke;
        public AktivitetStatus status;
        public String fraDato;
        public String tilDato;

        public AktivitetDTO toDTO(Extrapolator extrapolator) {
            return new AktivitetDTO()
                    .setType(type)
                    .setTittel(tittel)
                    .setBeskrivelse(extrapolator.extrapolate(beskrivelse))
                    .setAntallStillingerSokes(antallStillingerSokes)
                    .setAvtaleOppfolging(avtaleOppfolging)
                    .setHensikt(extrapolator.extrapolate(hensikt))
                    .setOppfolging(extrapolator.extrapolate(oppfolging))
                    .setLenke(extrapolator.extrapolate(lenke))
                    .setStatus(status)
                    .setFraDato(deserializeToDate(extrapolator.extrapolate(fraDato)))
                    .setTilDato(deserializeToDate(extrapolator.extrapolate(tilDato)));
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OppfolgingData {
        public OppfolgingDataFraFeed feedelement;
        public String predefineddataName;
    }
}
