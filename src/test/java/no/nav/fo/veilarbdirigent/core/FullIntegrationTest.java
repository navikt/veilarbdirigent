package no.nav.fo.veilarbdirigent.core;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.Map;
import lombok.SneakyThrows;
import no.nav.fo.feed.common.FeedElement;
import no.nav.fo.feed.common.FeedResponse;
import no.nav.fo.veilarbaktivitet.domain.AktivitetDTO;
import no.nav.fo.veilarbdialog.domain.NyHenvendelseDTO;
import no.nav.fo.veilarbdirigent.TestUtils;
import no.nav.fo.veilarbdirigent.config.AbstractIntegrationTest;
import no.nav.fo.veilarbdirigent.config.ApplicationConfig;
import no.nav.fo.veilarbdirigent.config.databasecleanup.TaskCleanup;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.fo.veilarbdirigent.utils.SerializerUtils;
import no.nav.testconfig.ApiAppTest;
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

import static java.lang.System.setProperty;
import static no.nav.dialogarena.config.fasit.FasitUtils.getDefaultEnvironment;
import static no.nav.dialogarena.config.fasit.FasitUtils.getRestService;
import static no.nav.fo.veilarbdirigent.TestUtils.delay;
import static no.nav.fo.veilarbdirigent.config.ApplicationConfig.APPLICATION_NAME;
import static no.nav.fo.veilarbdirigent.input.feed.OppfolgingFeedConsumerConfig.VEILARBOPPFOLGINGAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.veilarbaktivitet.MalverkService.VEILARBMALVERKAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbaktivitetService.VEILARBAKTIVITETAPI_URL_PROPERTY;
import static no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbdialogService.VEILARBDIALOGAPI_URL_PROPERTY;
import static no.nav.testconfig.ApiAppTest.Config.builder;
import static org.assertj.core.api.Java6Assertions.assertThat;

class FullIntegrationTest extends AbstractIntegrationTest implements TaskCleanup {

    private static final String AKTOR_ID = "123412341234";
    private static final String AUTOMATISK_QUERYPARAM = "&automatisk=true";
    private static MockWebServer providerServer;
    private static MockWebServer receiverServer;
    private static MockWebServer dialogreceiverServer;
    private static MockWebServer malverkServer;

    @BeforeAll
    @BeforeClass
    static void setupContext() {
        ApiAppTest.setupTestContext(builder().applicationName(APPLICATION_NAME).build());
        TestUtils.setupSecurity();
        setProperty("oidc-redirect.url", getRestService("veilarblogin.redirect-url", getDefaultEnvironment()).getUrl());
        providerServer = setupFeedProvider();
        receiverServer = setupReceiverSystem();
        dialogreceiverServer = setupDialogReceiverSystem();
        malverkServer = setupMalverkService();

        setProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY, providerServer.url("").toString());
        setProperty(VEILARBAKTIVITETAPI_URL_PROPERTY, receiverServer.url("").toString());
        setProperty(VEILARBDIALOGAPI_URL_PROPERTY, dialogreceiverServer.url("").toString());
        setProperty(VEILARBMALVERKAPI_URL_PROPERTY, malverkServer.url("").toString());

        setupContext(false, ApplicationConfig.class);
    }

    @Test
    void full_integration_test() throws InterruptedException {
        TaskDAO dao = getBean(TaskDAO.class);

        providerServer.takeRequest();
        takeAndVerifyReceiver();
        takeAndVerifyReceiver();
        takeAndVerifyReceiver();
        takeAndVerifyDialogReceiver();

        delay(100);

        Map<String, Integer> status = dao.fetchStatusnumbers();
        assertThat(status.getOrElse("OK", 0)).isEqualTo(5);
        assertThat(status.getOrElse("FAILED", 0)).isEqualTo(0);
    }

    @SneakyThrows
    private void takeAndVerifyReceiver() {
        Tuple3<String, String, AktivitetDTO> request = getData(receiverServer.takeRequest(5, TimeUnit.SECONDS), AktivitetDTO.class);
        assertThat(request._1).endsWith(AKTOR_ID + AUTOMATISK_QUERYPARAM);
        assertThat(request._2).isEqualTo("POST");
        assertThat(request._3.tittel).isNotBlank();
    }

    @SneakyThrows
    private void takeAndVerifyDialogReceiver() {
        Tuple3<String, String, NyHenvendelseDTO> request = getData(dialogreceiverServer.takeRequest(5, TimeUnit.SECONDS), NyHenvendelseDTO.class);
        assertThat(request._1).endsWith(AKTOR_ID);
        assertThat(request._2).isEqualTo("POST");
        assertThat(request._3.tekst).isNotBlank();
    }

    @SneakyThrows
    private static MockWebServer setupFeedProvider() {
        MockWebServer server = new MockWebServer();
        FeedElement<OppfolgingDataFraFeed> element = new FeedElement<>();
        element.setId("1000");
        element.setElement(new OppfolgingDataFraFeed(1000, AKTOR_ID,
                "BEHOV_FOR_ARBEIDSEVNEVURDERING", null, "SKAL_TIL_NY_ARBEIDSGIVER"));

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
    private static MockWebServer setupDialogReceiverSystem() {
        MockWebServer server = new MockWebServer();
        Dispatcher dispatcher = new Dispatcher() {
            AtomicInteger id = new AtomicInteger(1);

            @Override
            @SneakyThrows
            public MockResponse dispatch(RecordedRequest request) {
                NyHenvendelseDTO nyHenvendelseDTO = SerializerUtils.mapper.readValue(request.getBody().clone().readUtf8(), NyHenvendelseDTO.class);
                nyHenvendelseDTO.setDialogId(String.format("000%d", id.incrementAndGet()));

                return new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setResponseCode(200)
                        .setBody(SerializerUtils.mapper.writeValueAsString(nyHenvendelseDTO));
            }
        };

        server.setDispatcher(dispatcher);
        return server;
    }

    @SneakyThrows
    private static MockWebServer setupMalverkService() {
        MockWebServer server = new MockWebServer();

        AktivitetDTO response = new AktivitetDTO();
        response.setTittel("tittel");
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
