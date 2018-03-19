package no.nav.fo.veilarbdirigent;

import io.vavr.collection.List;
import lombok.SneakyThrows;
import no.nav.fo.veilarbdirigent.core.api.Status;
import no.nav.fo.veilarbdirigent.core.api.Task;
import no.nav.fo.veilarbdirigent.core.api.TaskType;
import no.nav.fo.veilarbdirigent.utils.TypedField;
import no.nav.sbl.jdbc.Transactor;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

public class TestUtils {
    public static TaskType TASK_TYPE = new TaskType("mock");
    public static Task createTask(String id, String data) {
        return Task
                .builder()
                .id(id)
                .type(TASK_TYPE)
                .status(Status.PENDING)
                .data(new TypedField<>(data))
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

    public static Transactor getTransactor() {
        return new Transactor(new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
                return null;
            }

            @Override
            public void commit(TransactionStatus status) throws TransactionException {

            }

            @Override
            public void rollback(TransactionStatus status) throws TransactionException {

            }
        });
    }
}
