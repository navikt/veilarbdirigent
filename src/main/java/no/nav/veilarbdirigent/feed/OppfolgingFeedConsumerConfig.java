package no.nav.veilarbdirigent.feed;

import no.nav.veilarbdirigent.feed.consumer.FeedConsumer;
import no.nav.veilarbdirigent.feed.consumer.FeedConsumerConfig;
import no.nav.veilarbdirigent.service.NyeBrukereFeedService;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;
import static no.nav.common.utils.UrlUtils.createServiceUrl;
import static no.nav.common.utils.UrlUtils.joinPaths;


@Configuration
public class OppfolgingFeedConsumerConfig {

    private final static String VEILARBOPPFOLGINGAPI_URL_PROPERTY = "VEILARBOPPFOLGINGAPI_URL";

    private final static int POLLING = 10;

    private final static String OPPFOLGING_FEED_NAME = "nyebrukere";

    private final String host;

    public OppfolgingFeedConsumerConfig() {
        String veilarboppfolgingUrl = joinPaths(createServiceUrl("veilarboppfolging", true), "/api");
        host = getOptionalProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY).orElse(veilarboppfolgingUrl);
    }

    @Bean
    public FeedConsumer oppfolgingFeedConsumer(NyeBrukereFeedService nyeBrukereFeedService, OkHttpClient client) {
        FeedConsumerConfig config = new FeedConsumerConfig(
                new FeedConsumerConfig.BaseConfig(
                        OppfolgingDataFraFeed.class,
                        () -> Long.toString(nyeBrukereFeedService.sisteKjenteId()),
                        host,
                        OPPFOLGING_FEED_NAME
                ),
                new FeedConsumerConfig.SimplePollingConfig(POLLING)
        )
                .restClient(client)
                .callback((lastId, list) -> nyeBrukereFeedService.processFeedElements(list));

        return new FeedConsumer(config);
    }
}
