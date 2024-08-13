package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbdirigent.repository.domain.OpprettAktivitetTaskDataV2;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskType;
import org.springframework.stereotype.Service;

import static no.nav.common.json.JsonUtils.fromJson;
import static no.nav.veilarbdirigent.utils.TaskFactory.AKTIVITET_TASK_TYPE_V2;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProcessorService {

    private final AktivitetService aktivitetService;

    public Try<String> processTask(Task task) {
        TaskType taskType = task.getType();
        if (AKTIVITET_TASK_TYPE_V2.equals(taskType)) {
            return processOpprettAktivitetTask(task);
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

    private Try<String> processOpprettAktivitetTaskData(OpprettAktivitetTaskDataV2 taskData) {
        log.info("Processing task {} aktorId={} malName={}", AKTIVITET_TASK_TYPE_V2, taskData.getAktorId(), taskData.getMalName());
        return aktivitetService.opprettAktivitetForBrukerMedMal(taskData.getAktorId(), taskData.getMalName());
    }
}
