package no.nav.fo.veilarbdirigent.core;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

class Utils {

    static void runWithLock(LockingTaskExecutor lock, String lockname, Runnable task) {
        Instant lockAtMostUntil = Instant.now().plusMillis(10000);
        LockConfiguration lockConfiguration = new LockConfiguration(lockname, lockAtMostUntil);
        lock.executeWithLock(task, lockConfiguration);
    }

    static void runInTransaction(TransactionTemplate template, Runnable runnable) {
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                runnable.run();
            }
        });
    }
}
