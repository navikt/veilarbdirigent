package no.nav.fo.veilarbdirigent.core;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.slf4j.MDC;

import java.time.Instant;

public class Utils {
    static void runWithLock(LockingTaskExecutor lock, String lockname, Runnable task) {
        Instant lockAtMostUntil = Instant.now().plusSeconds(120);
        LockConfiguration lockConfiguration = new LockConfiguration(lockname, lockAtMostUntil);
        lock.executeWithLock(task, lockConfiguration);
    }


    public static void runInMappedDiagnosticContext(String key, String value, Runnable func){
        try {
            MDC.put(key,value);
            func.run();
        }
        finally {
            MDC.remove(key);
        }

    }
}
