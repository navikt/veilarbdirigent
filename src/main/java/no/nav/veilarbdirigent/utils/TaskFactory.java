package no.nav.veilarbdirigent.utils;

import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.repository.domain.OpprettAktivitetTaskDataV2;
import no.nav.veilarbdirigent.repository.domain.OpprettDialogTaskDataV2;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskType;

import static no.nav.common.json.JsonUtils.toJson;

public class TaskFactory {

    private final static String CV_JOBBPROFIL_AKTIVITET_TASK_ID_SUFFIX = "cv_jobbprofil_aktivitet";

    private final static String JOBBSOKERKOMPETANSE_AKTIVITET_TASK_ID_SUFFIX = "jobbsokerkompetanse";

    private final static String KANSKJE_PERMITTERT_DIALOG_TASK_ID_SUFFIX = "kanskjePermitert";


    private final static String CV_JOBBPROFIL_AKTIVITET_MAL = "cv_jobbprofil_aktivitet";

    private final static String JOBBSOKERKOMPETANSE_AKTIVITET_MAL = "jobbsokerkompetanse_aktivitet";


    public final static TaskType AKTIVITET_TASK_TYPE_V1 = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET");

    public final static TaskType DIALOG_TASK_TYPE_V1 = TaskType.of("OPPFOLGING_OPPRETT_DIALOG");

    public final static TaskType AKTIVITET_TASK_TYPE_V2 = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET_v2");

    public final static TaskType DIALOG_TASK_TYPE_V2 = TaskType.of("OPPFOLGING_OPPRETT_DIALOG_v2");


    private final static String KANSKJE_PERMITTERT_DIALOG_NAME = "kanskje_permitert_dialog";


    public static Task lagCvJobbprofilAktivitetTask(long id, AktorId aktorId) {
        return new Task()
                .withId(id + CV_JOBBPROFIL_AKTIVITET_TASK_ID_SUFFIX)
                .withType(AKTIVITET_TASK_TYPE_V2)
                .withJsonData(toJson(new OpprettAktivitetTaskDataV2(aktorId, CV_JOBBPROFIL_AKTIVITET_MAL)));
    }

    public static Task lagJobbsokerkompetanseAktivitetTask(long id, AktorId aktorId) {
        return new Task()
                .withId(id + JOBBSOKERKOMPETANSE_AKTIVITET_TASK_ID_SUFFIX)
                .withType(AKTIVITET_TASK_TYPE_V2)
                .withJsonData(toJson(new OpprettAktivitetTaskDataV2(aktorId, JOBBSOKERKOMPETANSE_AKTIVITET_MAL)));
    }

    public static Task lagKanskjePermittertDialogTask(long id, AktorId aktorId) {
        return new Task()
                .withId(id + KANSKJE_PERMITTERT_DIALOG_TASK_ID_SUFFIX)
                .withType(DIALOG_TASK_TYPE_V2)
                .withJsonData(toJson(new OpprettDialogTaskDataV2(aktorId, KANSKJE_PERMITTERT_DIALOG_NAME)));
    }

}
