package no.nav.veilarbdirigent;

import io.vavr.collection.List;
import lombok.SneakyThrows;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import no.nav.veilarbdirigent.repository.domain.TaskType;
import org.mockito.ArgumentCaptor;

public class TestUtils {

    public static TaskType TASK_TYPE = new TaskType("mock");

    public static Task createTask(String id, String data) {
        return Task
                .builder()
                .id(id)
                .type(TASK_TYPE)
                .taskStatus(TaskStatus.PENDING)
                .jsonData(data)
                .build();
    }

    public static <T> ArgumentCaptor<List<T>> listArgumentCaptor(Class<T> cls) {
        Class<List<T>> captorType = (Class<List<T>>)(Class)List.class;
        return ArgumentCaptor.forClass(captorType);
    }

    @SneakyThrows
    public static void delay(int millis) {
        Thread.sleep(millis);
    }

}
