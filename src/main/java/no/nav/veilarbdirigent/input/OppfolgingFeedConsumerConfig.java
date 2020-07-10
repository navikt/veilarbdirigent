package no.nav.veilarbdirigent.input;

import io.vavr.collection.List;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.fo.feed.common.OutInterceptor;
import no.nav.fo.feed.consumer.FeedConsumer;
import no.nav.fo.feed.consumer.FeedConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Invocation;
import java.util.Collections;
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
    public FeedConsumer<OppfolgingDataFraFeed> oppfolgingFeedConsumer(OppfolgingFeedService service, SystemUserTokenProvider tokenProvider) {
        FeedConsumerConfig<OppfolgingDataFraFeed> config = new FeedConsumerConfig<>(
                new FeedConsumerConfig.BaseConfig<>(
                        OppfolgingDataFraFeed.class,
                        () -> Long.toString(service.sisteKjenteId()),
                        host,
                        OPPFOLGING_FEED_NAME
                ),
                new FeedConsumerConfig.SimplePollingConfig(POLLING)
        )
                .callback((lastId, list) -> service.compute(lastId, List.ofAll(list)))
                .interceptors(Collections.singletonList(new OidcFeedOutInterceptor(tokenProvider)));

        return new FeedConsumer<>(config);
    }

    public static class OidcFeedOutInterceptor implements OutInterceptor {
        private final SystemUserTokenProvider systemUserTokenProvider;
        public OidcFeedOutInterceptor(SystemUserTokenProvider systemUserTokenProvider) {
            this.systemUserTokenProvider = systemUserTokenProvider;
        }

        @Override
        public void apply(Invocation.Builder builder) {
            builder.header("Authorization", "Bearer " + systemUserTokenProvider.getSystemUserToken());
        }
    }
}
