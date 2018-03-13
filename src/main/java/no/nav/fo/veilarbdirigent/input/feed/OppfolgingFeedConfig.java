package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import no.nav.brukerdialog.security.oidc.OidcFeedOutInterceptor;
import no.nav.fo.feed.consumer.FeedConsumer;
import no.nav.fo.feed.consumer.FeedConsumerConfig;
import no.nav.fo.veilarbdirigent.core.CoreIn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;

@Configuration
public class OppfolgingFeedConfig {

    @Value("${veilarboppfolging.api.url}")
    private String host;

    @Value("${feed.consumer.pollingrate.millis:10000}")
    private int polling;

    @Value("${feed.consumer.lock.timeout.millis:5000}")
    private int lockTimeout;

    private static final String OPPFOLGING_FEED_NAME = "nyebrukere";

    @Bean
    public FeedDAO feedDAO(DataSource ds, JdbcTemplate jdbc){
        return new FeedDAO(ds, jdbc);
    }

    @Bean
    public OppfolgingFeedService oppfolgingFeedService(CoreIn core, FeedDAO dao){
        return new OppfolgingFeedService(core, dao);
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcLockProvider(dataSource);
    }

    @Bean
    public FeedConsumer<OppfolgingDataFraFeed> oppfolgingFeedConsumer(OppfolgingFeedService service, LockProvider lock) {
        FeedConsumerConfig<OppfolgingDataFraFeed> config = new FeedConsumerConfig<>(
                new FeedConsumerConfig.BaseConfig<>(
                        OppfolgingDataFraFeed.class,
                        () -> Long.toString(service.sisteKjenteId()),
                        host,
                        OPPFOLGING_FEED_NAME
                ),
                new FeedConsumerConfig.SimplePollingConfig(polling)
        )
                .lockProvider(lock, lockTimeout)
                .callback((lastId, list) -> service.compute(lastId, List.ofAll(list)))
                .interceptors(Collections.singletonList(new OidcFeedOutInterceptor()));

        return new FeedConsumer<>(config);
    }
}
