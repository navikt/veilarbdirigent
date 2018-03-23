package no.nav.fo.veilarbdirigent.core;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

class Utils {
    static void runWithLock(LockingTaskExecutor lock, String lockname, Runnable task) {
        Instant lockAtMostUntil = Instant.now().plusSeconds(120);
        LockConfiguration lockConfiguration = new LockConfiguration(lockname, lockAtMostUntil);
        lock.executeWithLock(task, lockConfiguration);
    }
}
