package no.nav.veilarbdirigent.utils;

import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.repository.domain.OpprettAktivitetTaskData;
import no.nav.veilarbdirigent.repository.domain.OpprettDialogTaskData;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskType;

public class TaskFactory {

    private final static String CV_JOBBPROFIL_AKTIVITET_TASK_ID_SUFFIX = "cv_jobbprofil_aktivitet";

    private final static String JOBBSOKERKOMPETANSE_AKTIVITET_TASK_ID_SUFFIX = "jobbsokerkompetanse";

    private final static String KANSKJE_PERMITTERT_DIALOG_TASK_ID_SUFFIX = "kanskjePermitert";


    private final static String CV_JOBBPROFIL_AKTIVITET_MAL = "cv_jobbprofil_aktivitet";

    private final static String JOBBSOKERKOMPETANSE_AKTIVITET_MAL = "jobbsokerkompetanse_aktivitet";


    public final static TaskType AKTIVITET_TASK_TYPE = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

    public final static TaskType DIALOG_TASK_TYPE = TaskType.of("OPPFOLGING_OPPRETT_DIALOG");


    private final static String KANSKJE_PERMITTERT_DIALOG_NAME = "kanskje_permitert_dialog";


    public static Task lagCvJobbprofilAktivitetTask(long id, AktorId aktorId) {
        return new Task<>()
                .withId(id + "_" + CV_JOBBPROFIL_AKTIVITET_TASK_ID_SUFFIX)
                .withType(AKTIVITET_TASK_TYPE) // TODO: Lagre med ny type
                .withData(new TypedField<>(new OpprettAktivitetTaskData(aktorId, CV_JOBBPROFIL_AKTIVITET_MAL)));
    }

    public static Task lagJobbsokerkompetanseAktivitetTask(long id, AktorId aktorId) {
        return new Task<>()
                .withId(id + "_" + JOBBSOKERKOMPETANSE_AKTIVITET_TASK_ID_SUFFIX)
                .withType(AKTIVITET_TASK_TYPE) // TODO: Lagre med ny type
                .withData(new TypedField<>(new OpprettAktivitetTaskData(aktorId, JOBBSOKERKOMPETANSE_AKTIVITET_MAL)));
    }

    public static Task lagKanskjePermittertDialogTask(long id, AktorId aktorId) {
        return new Task<>()
                .withId(id + "_" + KANSKJE_PERMITTERT_DIALOG_TASK_ID_SUFFIX)
                .withType(DIALOG_TASK_TYPE) // TODO: Lagre med ny type
                .withData(new TypedField<>(new OpprettDialogTaskData(aktorId, KANSKJE_PERMITTERT_DIALOG_NAME)));
    }

}
