package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import no.nav.common.types.identer.AktorId;
import no.nav.veilarbdirigent.repository.domain.*;
import org.springframework.stereotype.Service;

import static no.nav.common.json.JsonUtils.fromJson;
import static no.nav.veilarbdirigent.utils.TaskFactory.*;

@Service
@RequiredArgsConstructor
public class TaskProcessorService {

    private final AktivitetService aktivitetService;

    private final DialogService dialogService;

    public Try<String> processTask(Task task) {
        TaskType taskType = task.getType();

        if (AKTIVITET_TASK_TYPE_V1.equals(taskType)) {
            OldTask oldTask = fromJson(task.getJsonData(), OldTask.class);
            OpprettAktivitetTaskDataV1 taskData = fromJson(oldTask.getElement(), OpprettAktivitetTaskDataV1.class);

            OpprettAktivitetTaskDataV2 taskDataV2 = new OpprettAktivitetTaskDataV2(
                    AktorId.of(taskData.getFeedelement().getAktorId()),
                    taskData.getPredefineddataName()
            );

            return processOpprettAktivitetTask(taskDataV2);
        } else if (DIALOG_TASK_TYPE_V1.equals(taskType)) {
            OldTask oldTask = fromJson(task.getJsonData(), OldTask.class);
            OpprettDialogTaskDataV1 taskData = fromJson(oldTask.getElement(), OpprettDialogTaskDataV1.class);

            OpprettDialogTaskDataV2 taskDataV2 = new OpprettDialogTaskDataV2(
                    AktorId.of(taskData.getFeedelement().getAktorId()),
                    taskData.getMeldingsName()
            );

            return processOpprettDialogTask(taskDataV2);
        } else if (AKTIVITET_TASK_TYPE_V2.equals(taskType)) {
            return processOpprettAktivitetTask(fromJson(task.getJsonData(), OpprettAktivitetTaskDataV2.class));
        } else if (DIALOG_TASK_TYPE_V2.equals(taskType)) {
            return processOpprettDialogTask(fromJson(task.getJsonData(), OpprettDialogTaskDataV2.class));
        } else {
            return Try.failure(new IllegalArgumentException("Unable to process task of type " + taskType));
        }
    }

    public Try<String> processOpprettAktivitetTask(OpprettAktivitetTaskDataV2 taskData) {
        return aktivitetService.opprettAktivitetForBrukerMedMal(taskData.getAktorId(), taskData.getMalName());
    }

    public Try<String> processOpprettDialogTask(OpprettDialogTaskDataV2 taskData) {
        return dialogService.opprettDialogHvisBrukerErPermittert(taskData.getAktorId());
    }

}
