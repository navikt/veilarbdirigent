package no.nav.veilarbdirigent.core;

import no.nav.common.log.MDCConstants;
import org.slf4j.MDC;

public class Utils {

    public static void runInMappedDiagnosticContext(String key, String value, Runnable func){
        try {
            MDC.put(key,value);
            MDC.put(MDCConstants.MDC_CALL_ID, value);
            func.run();
        }
        finally {
            MDC.remove(key);
            MDC.remove(MDCConstants.MDC_CALL_ID);
        }

    }
}
