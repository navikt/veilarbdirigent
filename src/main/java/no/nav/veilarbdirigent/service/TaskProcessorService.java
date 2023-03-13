package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbdirigent.repository.domain.OpprettAktivitetTaskDataV2;
import no.nav.veilarbdirigent.repository.domain.OpprettDialogTaskDataV2;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskType;
import org.springframework.stereotype.Service;

import static no.nav.common.json.JsonUtils.fromJson;
import static no.nav.veilarbdirigent.utils.TaskFactory.AKTIVITET_TASK_TYPE_V2;
import static no.nav.veilarbdirigent.utils.TaskFactory.DIALOG_TASK_TYPE_V2;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProcessorService {

    private final AktivitetService aktivitetService;

    private final DialogService dialogService;

    public Try<String> processTask(Task task) {
        TaskType taskType = task.getType();
        if (AKTIVITET_TASK_TYPE_V2.equals(taskType)) {
            return processOpprettAktivitetTask(task);
        } else if (DIALOG_TASK_TYPE_V2.equals(taskType)) {
            return processOpprettDialogTask(task);
        } else {
            String message = "Unable to process task of type " + taskType;
            log.error(message);
            return Try.failure(new IllegalArgumentException(message));
        }
    }

    public Try<String> processOpprettAktivitetTask(Task opprettAktivitetTaskV2) {
        try {
            OpprettAktivitetTaskDataV2 taskData = fromJson(opprettAktivitetTaskV2.getJsonData(), OpprettAktivitetTaskDataV2.class);
            return processOpprettAktivitetTaskData(taskData);
        } catch (Exception e) {
            log.error("Failed to process " + AKTIVITET_TASK_TYPE_V2, e);
            return Try.failure(e);
        }
    }

    public Try<String> processOpprettDialogTask(Task opprettDialogTaskV2) {
        try {
            OpprettDialogTaskDataV2 taskData = fromJson(opprettDialogTaskV2.getJsonData(), OpprettDialogTaskDataV2.class);
            return processOpprettDialogTaskData(taskData);
        } catch (Exception e) {
            log.error("Failed to process " + DIALOG_TASK_TYPE_V2, e);
            return Try.failure(e);
        }
    }

    private Try<String> processOpprettAktivitetTaskData(OpprettAktivitetTaskDataV2 taskData) {
        log.info("Processing task {} aktorId={} malName={}", AKTIVITET_TASK_TYPE_V2, taskData.getAktorId(), taskData.getMalName());
        return aktivitetService.opprettAktivitetForBrukerMedMal(taskData.getAktorId(), taskData.getMalName());
    }

    private Try<String> processOpprettDialogTaskData(OpprettDialogTaskDataV2 taskData) {
        log.info("Processing task {} aktorId={} dialogName={}", DIALOG_TASK_TYPE_V2, taskData.getAktorId(), taskData.getDialogName());
        return dialogService.opprettDialogHvisBrukerErPermittert(taskData.getAktorId());
    }

}
