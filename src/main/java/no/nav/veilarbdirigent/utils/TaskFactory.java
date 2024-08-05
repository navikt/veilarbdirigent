package no.nav.veilarbdirigent.utils;

import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.repository.domain.OpprettAktivitetTaskDataV2;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskType;

import java.util.UUID;

import static no.nav.common.json.JsonUtils.toJson;

public class TaskFactory {

    private final static String CV_JOBBPROFIL_AKTIVITET_TASK_ID_SUFFIX = "cv_jobbprofil_aktivitet";

    private final static String CV_JOBBPROFIL_AKTIVITET_MAL = "cv_jobbprofil_aktivitet";

    public final static TaskType AKTIVITET_TASK_TYPE_V2 = TaskType.of("OPPFOLGING_OPPRETT_AKTIVITET_v2");

    public static Task lagCvJobbprofilAktivitetTask(UUID oppfolgingsPeriodeId, AktorId aktorId) {
        return new Task()
                .withId(oppfolgingsPeriodeId + "_" + CV_JOBBPROFIL_AKTIVITET_TASK_ID_SUFFIX)
                .withType(AKTIVITET_TASK_TYPE_V2)
                .withJsonData(toJson(new OpprettAktivitetTaskDataV2(aktorId, CV_JOBBPROFIL_AKTIVITET_MAL)));
    }
}
