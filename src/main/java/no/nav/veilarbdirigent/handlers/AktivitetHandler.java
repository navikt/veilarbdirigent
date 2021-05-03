package no.nav.veilarbdirigent.handlers;

import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.core.api.*;
import no.nav.veilarbdirigent.input.OppfolgingDataFraFeed;
import no.nav.veilarbdirigent.utils.TypedField;

import javax.annotation.PostConstruct;

public class AktivitetHandler implements MessageHandler, Actuator<AktivitetHandler.OppfolgingDataMedMal, String> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

    private Core core;
    private VeilarbaktivitetClient veilarbaktivitetClient;
    private VeilarbmalverkClient veilarbmalverkClient;

    private final List<String> registeringForslag = List.of("STANDARD_INNSATS",
            "SITUASJONSBESTEMT_INNSATS",
            "BEHOV_FOR_ARBEIDSEVNEVURDERING");

    private final List<String> sykmeldtBrukerTyper = List.of("SKAL_TIL_NY_ARBEIDSGIVER");

    public AktivitetHandler(Core core, VeilarbaktivitetClient veilarbaktivitetClient, VeilarbmalverkClient veilarbmalverkClient) {
        this.core = core;
        this.veilarbaktivitetClient = veilarbaktivitetClient;
        this.veilarbmalverkClient = veilarbmalverkClient;
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

            boolean erNySykmeldtBrukerRegistrert = sykmeldtBrukerTyper.contains(msg.getSykmeldtBrukerType());
            boolean erNyRegistrert = registeringForslag.contains(msg.getForeslattInnsatsgruppe());

            if (erNySykmeldtBrukerRegistrert || erNyRegistrert) {
                return aktivitetListe(msg);
            }

            return List.empty();
        } else {
            return List.empty();
        }
    }

    private List<Task> aktivitetListe(OppfolgingDataFraFeed msg) {
        // The order decides in what order the tasks are executed. This the required order.
        // The last activity created is the first shown in he same column.
        return List.of(
               new Task<>()
                        .withId(msg.getId() + "cv_jobbprofil_aktivitet")
                        .withType(TYPE)
                        .withData(new TypedField<>(new OppfolgingDataMedMal(msg, "cv_jobbprofil_aktivitet"))),
                new Task<>()
                        .withId(msg.getId() + "jobbsokerkompetanse")
                        .withType(TYPE)
                        .withData(new TypedField<>(new OppfolgingDataMedMal(msg, "jobbsokerkompetanse_aktivitet")))
        );
    }

    @Override
    public Try<String> handle(OppfolgingDataMedMal data) {
        return veilarbmalverkClient.hentMal(data.predefineddataName)
                .flatMap((template) -> veilarbaktivitetClient.lagAktivitet(AktorId.of(data.feedelement.getAktorId()), template));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OppfolgingDataMedMal {
        public OppfolgingDataFraFeed feedelement;
        public String predefineddataName;
    }
}
