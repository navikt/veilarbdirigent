package no.nav.fo.veilarbdirigent.input.feed;

import io.vavr.collection.List;
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

    @Value("${feed.consumer.pollingrate:*/10 * * * 2 ?}")
    private String polling;

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
    public FeedConsumer<OppfolgingDataFraFeed> oppfolgingFeedConsumer(OppfolgingFeedService service) {
        FeedConsumerConfig<OppfolgingDataFraFeed> config = new FeedConsumerConfig<>(
                new FeedConsumerConfig.BaseConfig<>(
                        OppfolgingDataFraFeed.class,
                        () -> Long.toString(service.sisteKjenteId()),
                        host,
                        OPPFOLGING_FEED_NAME
                ),
                new FeedConsumerConfig.PollingConfig(polling)
        )
                .callback((lastId, list) -> service.compute(lastId, List.ofAll(list)))
                .interceptors(Collections.singletonList(new OidcFeedOutInterceptor()));

        return new FeedConsumer<>(config);
    }
}
