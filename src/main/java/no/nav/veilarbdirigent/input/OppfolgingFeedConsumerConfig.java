package no.nav.veilarbdirigent.input;

import io.vavr.collection.List;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.veilarbdirigent.feed.consumer.FeedConsumer;
import no.nav.veilarbdirigent.feed.consumer.FeedConsumerConfig;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;
import static no.nav.common.utils.UrlUtils.clusterUrlForApplication;


@Configuration
public class OppfolgingFeedConsumerConfig {

    public static final String VEILARBOPPFOLGINGAPI_URL_PROPERTY = "VEILARBOPPFOLGINGAPI_URL";
    private static final int POLLING = 10;
    private static final String OPPFOLGING_FEED_NAME = "nyebrukere";

    private final String host;

    public OppfolgingFeedConsumerConfig() {
        Supplier<String> naisUrl = () -> clusterUrlForApplication("veilarboppfolging") + "/veilarboppfolging/api";
        host = getOptionalProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY).orElseGet(naisUrl);
    }

    @Bean
    public FeedConsumer oppfolgingFeedConsumer(OppfolgingFeedService service,
                                               OkHttpClient client,
                                               SystemUserTokenProvider tokenProvider) {
        FeedConsumerConfig config = new FeedConsumerConfig(
                new FeedConsumerConfig.BaseConfig(
                        OppfolgingDataFraFeed.class,
                        () -> Long.toString(service.sisteKjenteId()),
                        host,
                        OPPFOLGING_FEED_NAME
                ),
                new FeedConsumerConfig.SimplePollingConfig(POLLING)
        )
                .restClient(client)
                .callback((lastId, list) -> service.compute(lastId, List.ofAll(list)));

        return new FeedConsumer(config);
    }
}
