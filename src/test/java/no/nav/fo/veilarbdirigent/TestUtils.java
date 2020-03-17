package no.nav.fo.veilarbdirigent;

import io.vavr.collection.List;
import lombok.SneakyThrows;
import no.nav.brukerdialog.security.Constants;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.fo.veilarbdirigent.core.api.Status;
import no.nav.fo.veilarbdirigent.core.api.Task;
import no.nav.fo.veilarbdirigent.core.api.TaskType;
import no.nav.fo.veilarbdirigent.utils.TypedField;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;
import no.nav.sbl.jdbc.Transactor;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import static java.lang.System.setProperty;

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

    public static void setupSecurity(){
        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, "https://localhost:6218");
        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, "123");
        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, "123");
        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, "https://localhost:6218");
        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, "https://localhost:6218");
        setProperty(Constants.ISSO_ISALIVE_URL_PROPERTY_NAME, "https://localhost:6218");
        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, "112");
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, "123");

        setProperty("oidc-redirect.url", "https://localhost:6218");
        setProperty(StsSecurityConstants.STS_URL_KEY, "https://localhost:6218");

    }
}
