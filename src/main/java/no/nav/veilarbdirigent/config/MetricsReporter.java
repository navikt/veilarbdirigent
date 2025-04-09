package no.nav.veilarbdirigent.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static io.micrometer.prometheusmetrics.PrometheusConfig.DEFAULT;


@Slf4j
@Component
public class MetricsReporter {

    private static final MeterRegistry prometheusMeterRegistry = new ProtectedPrometheusMeterRegistry();

    public static MeterRegistry getMeterRegistry() {
        return prometheusMeterRegistry;
    }

    public static class ProtectedPrometheusMeterRegistry extends PrometheusMeterRegistry {
        public ProtectedPrometheusMeterRegistry() {
            super(DEFAULT);
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    }
}
