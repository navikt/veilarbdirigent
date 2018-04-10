package no.nav.fo.veilarbdirigent.core;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.Map;
import lombok.SneakyThrows;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.fo.feed.common.FeedElement;
import no.nav.fo.feed.common.FeedResponse;
import no.nav.fo.veilarbaktivitet.domain.AktivitetDTO;
import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.config.AbstractIntegrationTest;
import no.nav.fo.veilarbdirigent.config.ApplicationConfig;
import no.nav.fo.veilarbdirigent.config.databasecleanup.TaskCleanup;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.fo.veilarbdirigent.utils.SerializerUtils;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static no.nav.fo.veilarbdirigent.TestUtils.delay;
import static no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConsumerConfig.VEILARBOPPFOLGINGAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.veilarbaktivitet.MalverkService.VEILARBMALVERK_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbaktivitetService.VEILARBAKTIVITETAPI_URL_PROPERTY;
import static org.assertj.core.api.Java6Assertions.assertThat;

class FullIntegrationTest extends AbstractIntegrationTest implements TaskCleanup {

    private static final String AKTOR_ID = "123412341234";
    private static MockWebServer providerServer;
    private static MockWebServer receiverServer;
    private static MockWebServer malverkServer;

    @BeforeAll
    @BeforeClass
    static void setupContext() {
        TestUtils.setupSecurity();
        System.setProperty("oidc-redirect.url", FasitUtils.getBaseUrl("veilarblogin.redirect-url", FasitUtils.Zone.FSS));
        providerServer = setupFeedProvider();
        receiverServer = setupReceiverSystem();
        malverkServer = setupMalverkService();

        System.setProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY, providerServer.url("").toString());
        System.setProperty(VEILARBAKTIVITETAPI_URL_PROPERTY, receiverServer.url("").toString());
        System.setProperty(VEILARBMALVERK_URL_PROPERTY, malverkServer.url("").toString());

        setupContext(false, ApplicationConfig.class);
    }

    @Test
    void full_integration_test() throws InterruptedException {
        TaskDAO dao = getBean(TaskDAO.class);

        providerServer.takeRequest();
        takeAndVerifyReceiver();
        takeAndVerifyReceiver();
        takeAndVerifyReceiver();
        takeAndVerifyReceiver();

        delay(100);

        Map<String, Integer> status = dao.fetchStatusnumbers();
        assertThat(status.getOrElse("OK", 0)).isEqualTo(4);
        assertThat(status.getOrElse("FAILED", 0)).isEqualTo(0);
    }

    @SneakyThrows
    private void takeAndVerifyReceiver() {
        Tuple3<String, String, AktivitetDTO> request = getData(receiverServer.takeRequest(20, TimeUnit.SECONDS), AktivitetDTO.class);
        assertThat(request._1).endsWith(AKTOR_ID);
        assertThat(request._2).isEqualTo("POST");
        assertThat(request._3.tittel).isNotBlank();
    }

    @SneakyThrows
    private static MockWebServer setupFeedProvider() {
        MockWebServer server = new MockWebServer();
        FeedElement<OppfolgingDataFraFeed> element = new FeedElement<>();
        element.setId("1000");
        element.setElement(new OppfolgingDataFraFeed(1000, AKTOR_ID, true, null));

        FeedResponse<OppfolgingDataFraFeed> response = new FeedResponse<>();
        response.setNextPageId("1000");
        response.setElements(Collections.singletonList(element));

        String json = SerializerUtils.mapper.writeValueAsString(response);

        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(json));

        return server;
    }

    @SneakyThrows
    private static MockWebServer setupReceiverSystem() {
        MockWebServer server = new MockWebServer();
        Dispatcher dispatcher = new Dispatcher() {
            AtomicInteger id = new AtomicInteger(1);

            @Override
            @SneakyThrows
            public MockResponse dispatch(RecordedRequest request) {
                AktivitetDTO aktivitetDTO = SerializerUtils.mapper.readValue(request.getBody().clone().readUtf8(), AktivitetDTO.class);
                aktivitetDTO.setId(String.format("000%d", id.incrementAndGet()));

                return new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setResponseCode(200)
                        .setBody(SerializerUtils.mapper.writeValueAsString(aktivitetDTO));
            }
        };

        server.setDispatcher(dispatcher);
        return server;
    }

    @SneakyThrows
    private static MockWebServer setupMalverkService() {
        MockWebServer server = new MockWebServer();

        AktivitetDTO response = new AktivitetDTO();
        String json = SerializerUtils.mapper.writeValueAsString(response);

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest recordedRequest) throws InterruptedException {
                return new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setResponseCode(200)
                        .setBody(json);
            }
        });

        return server;
    }

    @SneakyThrows
    private static <T> Tuple3<String, String, T> getData(RecordedRequest request, Class<T> cls) {
        return Tuple.of(
                request.getPath(),
                request.getMethod(),
                SerializerUtils.mapper.readValue(request.getBody().readUtf8(), cls)
        );
    }

}
