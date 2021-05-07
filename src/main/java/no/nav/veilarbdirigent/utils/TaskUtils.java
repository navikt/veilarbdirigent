package no.nav.veilarbdirigent.utils;

import io.vavr.control.Try;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;

public class TaskUtils {

    public static TaskStatus getStatusFromTry(Try<?> tryResult) {
        return tryResult.isSuccess() ? TaskStatus.OK : TaskStatus.FAILED;
    }

}
