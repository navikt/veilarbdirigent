package no.nav.veilarbdirigent.feed.consumer;

import lombok.SneakyThrows;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbdirigent.feed.common.*;
import no.nav.veilarbdirigent.input.OppfolgingDataFraFeed;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import javax.ws.rs.client.Entity;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.veilarbdirigent.feed.consumer.FeedPoller.createScheduledJob;
import static no.nav.veilarbdirigent.feed.util.UrlUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public class FeedConsumer implements Authorization, ApplicationListener<ContextClosedEvent> {
    private static final Logger LOG = getLogger(FeedConsumer.class);

    private final FeedConsumerConfig config;
    private int lastResponseHash;

    public FeedConsumer(FeedConsumerConfig config) {
        String feedName = config.feedName;
        String host = config.host;

        this.config = config;

        createScheduledJob(feedName, host, config.pollingConfig, runWithLock(feedName, this::poll));
        createScheduledJob(feedName + "/webhook", host, config.webhookPollingConfig, this::registerWebhook);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        FeedPoller.shutdown();
    }

    public boolean webhookCallback() {
        if (this.config.webhookPollingConfig == null) {
            return false;
        }

        CompletableFuture.runAsync(runWithLock(this.config.feedName, this::poll));
        return true;
    }

    @SneakyThrows
    void registerWebhook() {
        String callbackUrl = callbackUrl(this.config.webhookPollingConfig.apiRootPath, this.config.feedName);
        FeedWebhookRequest body = new FeedWebhookRequest().setCallbackUrl(callbackUrl);

        Entity<FeedWebhookRequest> entity = Entity.entity(body, APPLICATION_JSON_TYPE);

        Request request = new Request.Builder()
                .url(asUrl(this.config.host, "feed", this.config.feedName, "webhook"))
                .build();

        try (Response response = this.config.client.newCall(request).execute()) {

            int responseStatus = response.code();
            if (responseStatus == 201) {
                LOG.info("Webhook opprettet hos produsent!");
            } else if (responseStatus != 200) {
                LOG.warn("Endepunkt for opprettelse av webhook returnerte feilkode {}", responseStatus);
            }
        }
    }

    @SneakyThrows
    public synchronized Response poll() {
        String lastEntry = this.config.lastEntrySupplier.get();
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(getTargetUrl())).newBuilder();
        httpBuilder.addQueryParameter(QUERY_PARAM_ID, lastEntry);
        httpBuilder.addQueryParameter(QUERY_PARAM_PAGE_SIZE, String.valueOf(this.config.pageSize));

        Request request = new Request.Builder().url(httpBuilder.build()).build();
        try (Response response = this.config.client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            FeedResponse entity = RestUtils.parseJsonResponse(response, FeedResponse.class).get();


            List<FeedElement> elements = entity.getElements();
            if (elements != null && !elements.isEmpty()) {
                List<OppfolgingDataFraFeed> data = elements
                        .stream()
                        .map(FeedElement::getElement)
                        .collect(Collectors.toList());

                if (!(entity.hashCode() == lastResponseHash)) {
                    this.config.callback.call(entity.getNextPageId(), data);
                }
                this.lastResponseHash = entity.hashCode();
            }


            return response;
        }
    }


    private String getTargetUrl() {
        return asUrl(this.config.host, "feed", this.config.feedName);
    }

    @Override
    public FeedAuthorizationModule getAuthorizationModule() {
        return config.authorizationModule;
    }

    private Runnable runWithLock(String lockname, Runnable task) {
        return task::run;
    }
}
