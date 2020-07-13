package no.nav.veilarbdirigent.core;

import org.slf4j.MDC;

public class Utils {

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
