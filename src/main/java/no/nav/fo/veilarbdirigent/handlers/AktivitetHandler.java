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

public class AktivitetHandler implements MessageHandler, Actuator<AktivitetHandler.OppfolgingDataMedMal, AktivitetDTO> {
    private final TaskType TYPE = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

    @Inject
    private Core core;
    @Inject
    private VeilarbaktivitetService service;
    @Inject
    private MalverkService malverk;

    private final List<String> registeringForslag = List.of("STANDARD_INNSATS",
            "SITUASJONSBESTEMT_INNSATS",
            "BEHOV_FOR_ARBEIDSEVNEVURDERING");

    private final List<String> sykmeldtBrukerTyper = List.of("SKAL_TIL_NY_ARBEIDSGIVER");

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
                        .withId(String.valueOf(msg.getId()) + "mia")
                        .withType(TYPE)
                        .withData(new TypedField<>(new OppfolgingDataMedMal(msg, "mulighet_i_arbeidsmarkedet_aktivitet"))),
                new Task<>()
                        .withId(String.valueOf(msg.getId()) + "cv_jobbprofil_aktivitet")
                        .withType(TYPE)
                        .withData(new TypedField<>(new OppfolgingDataMedMal(msg, "cv_jobbprofil_aktivitet"))),
                new Task<>()
                        .withId(String.valueOf(msg.getId()) + "jobbsokerkompetanse")
                        .withType(TYPE)
                        .withData(new TypedField<>(new OppfolgingDataMedMal(msg, "jobbsokerkompetanse_aktivitet")))
        );
    }

    @Override
    public Try<AktivitetDTO> handle(OppfolgingDataMedMal data) {
        return malverk.hentMal(data.predefineddataName)
                .flatMap((template) -> service.lagAktivitet(data.feedelement.getAktorId(), template));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OppfolgingDataMedMal {
        public OppfolgingDataFraFeed feedelement;
        public String predefineddataName;
    }
}
