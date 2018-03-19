package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.brukerdialog.security.oidc.OidcFeedOutInterceptor;
import no.nav.fo.feed.consumer.FeedConsumer;
import no.nav.fo.feed.consumer.FeedConsumerConfig;
import no.nav.fo.veilarbdirigent.core.Core;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;

@Configuration
public class OppfolgingFeedConfig {

    @Value("${veilArbOppfolgingAPI.url}")
    private String host;

    @Value("${feed.consumer.pollingrate.seconds:10}")
    private int polling;

    @Value("${feed.consumer.lock.timeout.millis:5000}")
    private int lockTimeout;

    private static final String OPPFOLGING_FEED_NAME = "nyebrukere";

    @Bean
    public FeedDAO feedDAO(JdbcTemplate jdbc) {
        return new FeedDAO(jdbc);
    }

    @Bean
    public OppfolgingFeedService oppfolgingFeedService(Core core, FeedDAO dao) {
        return new OppfolgingFeedService(core, dao);
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
                new FeedConsumerConfig.SimplePollingConfig(polling)
        )
                .lockExecutor(lock, lockTimeout)
                .callback((lastId, list) -> service.compute(lastId, List.ofAll(list)))
                .interceptors(Collections.singletonList(new OidcFeedOutInterceptor()));

        return new FeedConsumer<>(config);
    }
}
