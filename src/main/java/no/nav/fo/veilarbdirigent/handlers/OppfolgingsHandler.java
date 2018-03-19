package no.nav.fo.veilarbdirigent.handlers;

import io.vavr.collection.List;
import io.vavr.control.Option;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.PredefinedDataLoader;
import no.nav.fo.veilarbdirigent.core.api.*;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.fo.veilarbdirigent.service.aktivitet.AktivitetData;
import no.nav.fo.veilarbdirigent.service.aktivitet.VeilarbaktivitetService;
import no.nav.fo.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class OppfolgingsHandler implements MessageHandler, Actuator<OppfolgingsHandler.OppfolgingData, AktivitetData> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

    @Inject
    Core core;
    @Inject
    VeilarbaktivitetService service;

    @PostConstruct
    public void register() {
        core.registerHandler(this);
    }


    @Override
    public List<Task> handle(Message message) {
        if (message instanceof OppfolgingDataFraFeed) {
            OppfolgingDataFraFeed msg = (OppfolgingDataFraFeed) message;
            return List.of(
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
    public Task<OppfolgingsHandler.OppfolgingData, AktivitetData> handle(Task<OppfolgingsHandler.OppfolgingData, AktivitetData> task) {
        OppfolgingData data = task.getData().element;
        Option<AktivitetData> maybeAktivitetData = PredefinedDataLoader.get(data.predefineddataName, AktivitetData.class);

        if (maybeAktivitetData.isEmpty()) {
            throw new RuntimeException("Could not find predefined data " + data.predefineddataName);
        }

        AktivitetData predefinertData = maybeAktivitetData.get();

        return service.lagAktivitet(predefinertData)
                .toTry()
                .map((result) -> task.withResult(new TypedField<>(result)))
                .get();
    }


    public static class OppfolgingData {
        public final OppfolgingDataFraFeed feedelement;
        public final String predefineddataName;

        public OppfolgingData(OppfolgingDataFraFeed feedelement, String predefineddataName) {
            this.feedelement = feedelement;
            this.predefineddataName = predefineddataName;
        }
    }
}
