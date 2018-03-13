package no.nav.fo.veilarbdirigent;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Status;
import no.nav.fo.veilarbdirigent.core.Task;
import org.mockito.ArgumentCaptor;

public class TestUtils {
    public static Task createTask(String id, String type, String data) {
        return Task
                .builder()
                .id(id)
                .type(type)
                .status(Status.PENDING)
                .data(data)
                .build();
    }

    public static <T> ArgumentCaptor<List<T>> listArgumentCaptor(Class<T> cls) {
        Class<List<T>> captorType = (Class<List<T>>)(Class)List.class;
        return ArgumentCaptor.forClass(captorType);
    }
}
