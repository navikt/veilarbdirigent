package no.nav.veilarbdirigent.utils;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class TaskUtils {

    public static TaskStatus getStatusFromTry(Try<?> tryResult) {
        return tryResult.isSuccess() ? TaskStatus.OK : TaskStatus.FAILED;
    }

    public static Optional<Task> createTaskIfNotStoredInDb(Supplier<Task> taskSupplier, TaskRepository taskRepository) {
        Task task = taskSupplier.get();

        if (taskRepository.hasTask(task.getId())) {
            log.info("Task already exists for id={}", task.getId());
            return Optional.empty();
        } else {
            log.info("Creating new task with id={}", task.getId());
            return Optional.of(task);
        }
    }

}
