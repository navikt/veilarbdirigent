package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.apiapp.util.UrlUtils;
import no.nav.brukerdialog.security.oidc.OidcFeedOutInterceptor;
import no.nav.fo.feed.consumer.FeedConsumer;
import no.nav.fo.feed.consumer.FeedConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.function.Supplier;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Configuration
public class OppfolgingFeedConsumerConfig {

    public static final String VEILARBOPPFOLGINGAPI_URL_PROPERTY = "VEILARBOPPFOLGINGAPI_URL";
    private static final int POLLING = 10;
    private static final int LOCK_TIMEOUT_MILLIS = 120_000;
    private static final String OPPFOLGING_FEED_NAME = "nyebrukere";

    private final String host;

    public OppfolgingFeedConsumerConfig() {
        Supplier<String> naisUrl = () -> UrlUtils.clusterUrlForApplication("veilarboppfolging") + "/veilarboppfolging/api";
        host = getOptionalProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    @Bean
    public FeedConsumer<OppfolgingDataFraFeed> oppfolgingFeedConsumer(OppfolgingFeedService service, LockingTaskExecutor lock) {
        FeedConsumerConfig<OppfolgingDataFraFeed> config = new FeedConsumerConfig<>(
                new FeedConsumerConfig.BaseConfig<>(
                        OppfolgingDataFraFeed.class,
                        () -> Long.toString(service.sisteKjenteId()),
                        host,
                        OPPFOLGING_FEED_NAME
                ),
                new FeedConsumerConfig.SimplePollingConfig(POLLING)
        )
                .lockExecutor(lock, LOCK_TIMEOUT_MILLIS)
                .callback((lastId, list) -> service.compute(lastId, List.ofAll(list)))
                .interceptors(Collections.singletonList(new OidcFeedOutInterceptor()));

        return new FeedConsumer<>(config);
    }
}
