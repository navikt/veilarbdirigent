package no.nav.fo.veilarbdirigent;

import io.vavr.collection.List;
import lombok.SneakyThrows;
import no.nav.fo.veilarbdirigent.core.Status;
import no.nav.fo.veilarbdirigent.core.Task;
import org.mockito.ArgumentCaptor;

public class TestUtils {
    public static String TASK_TYPE = "mock";
    public static Task createTask(String id, String data) {
        return Task
                .builder()
                .id(id)
                .type(TASK_TYPE)
                .status(Status.PENDING)
                .data(data)
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
