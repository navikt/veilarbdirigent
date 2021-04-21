package no.nav.veilarbdirigent.utils;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;

public class MetricsUtils {

    private final static String METRICS_PREFIX = getOptionalProperty("APP_NAME")
            .orElse("veilarbdirigent");

    public static String metricName(String name) {
        return String.format("%s.%s", METRICS_PREFIX, name);
    }

}
