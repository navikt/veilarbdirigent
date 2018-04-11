package no.nav.fo.veilarbdirigent.utils;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class MetricsUtils {
    private final static String METRICS_PREFIX = getOptionalProperty("APP_NAME")
            .orElse("veilarbmalverk");

    public static String metricName(String name) {
        return String.format("%s.%s", METRICS_PREFIX, name);
    }
}
