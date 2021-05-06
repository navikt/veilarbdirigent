package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import no.nav.veilarbdirigent.repository.domain.OpprettAktivitetTaskData;
import no.nav.veilarbdirigent.repository.domain.OpprettDialogTaskData;
import no.nav.veilarbdirigent.repository.domain.Task;
import org.springframework.stereotype.Service;

import static no.nav.veilarbdirigent.utils.TaskFactory.AKTIVITET_TASK_TYPE;
import static no.nav.veilarbdirigent.utils.TaskFactory.DIALOG_TASK_TYPE;

@Service
@RequiredArgsConstructor
public class TaskProcessorService {

    private final AktivitetService aktivitetService;

    private final DialogService dialogService;

    public Try<String> processTask(Task task) {
        // TODO: Håndter gamle typer fra tidligere tasks

        if (AKTIVITET_TASK_TYPE.equals(task.getType())) {
            return processOpprettAktivitetTask(task);
        } else if (DIALOG_TASK_TYPE.equals(task.getType())) {
            return processOpprettDialogTask(task);
        } else {
            return Try.failure(new IllegalArgumentException("Unable to process task of type " + task.getType()));
        }
    }

    public Try<String> processOpprettAktivitetTask(Task opprettAktivitetTask) {
        OpprettAktivitetTaskData taskData = (OpprettAktivitetTaskData) opprettAktivitetTask.getData().element;
        return aktivitetService.opprettAktivitetForBrukerMedMal(taskData.getAktorId(), taskData.getMalName());
    }

    public Try<String> processOpprettDialogTask(Task opprettDialogTask) {
        OpprettDialogTaskData taskData = (OpprettDialogTaskData) opprettDialogTask.getData().element;
        return dialogService.opprettDialogHvisBrukerErPermittert(taskData.getAktorId());
    }

}
