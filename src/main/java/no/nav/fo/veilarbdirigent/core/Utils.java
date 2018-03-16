package no.nav.fo.veilarbdirigent.core;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;

import java.time.Instant;

public class Utils {

    public static void runWithLock(LockingTaskExecutor lock, String lockname, Runnable task) {
        Instant lockAtMostUntil = Instant.now().plusMillis(10000);
        LockConfiguration lockConfiguration = new LockConfiguration(lockname, lockAtMostUntil);
        lock.executeWithLock(task, lockConfiguration);
    }
}
