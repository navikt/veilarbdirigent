package no.nav.veilarbdirigent.core;

import org.junit.jupiter.api.Disabled;


//TODO: Mock this better
@Disabled
class FullIntegrationTest  {

    /*private static final String AKTOR_ID = "123412341234";
    private static final String AUTOMATISK_QUERYPARAM = "&automatisk=true";
    private static MockWebServer providerServer;
    private static MockWebServer receiverServer;
    private static MockWebServer dialogreceiverServer;
    private static MockWebServer malverkServer;

    @BeforeAll
    @BeforeClass
    static void setupContext() {
        //ApiAppTest.setupTestContext(builder().applicationName(APPLICATION_NAME).build());
        setProperty("NAIS_APP_NAME", APPLICATION_NAME);

        TestUtils.setupSecurity();
        providerServer = setupFeedProvider();
        receiverServer = setupReceiverSystem();
        dialogreceiverServer = setupDialogReceiverSystem();
        malverkServer = setupMalverkService();

        setProperty(OppfolgingFeedConsumerConfig.VEILARBOPPFOLGINGAPI_URL_PROPERTY, providerServer.url("").toString());
        setProperty(VeilarbaktivitetService.VEILARBAKTIVITETAPI_URL_PROPERTY, receiverServer.url("").toString());
        setProperty(VeilarbdialogService.VEILARBDIALOGAPI_URL_PROPERTY, dialogreceiverServer.url("").toString());
        setProperty(MalverkService.VEILARBMALVERKAPI_URL_PROPERTY, malverkServer.url("").toString());

        setupContext(false, ApplicationConfig.class);
    }

    @Test
    void full_integration_test() throws InterruptedException {
        TaskDAO dao = getBean(TaskDAO.class);

        providerServer.takeRequest();
        takeAndVerifyReceiver();
        takeAndVerifyReceiver();
        takeAndVerifyDialogReceiver();

        delay(100);

        Map<String, Integer> status = dao.fetchStatusnumbers();
        assertThat(status.getOrElse("OK", 0)).isEqualTo(3);
        assertThat(status.getOrElse("FAILED", 0)).isEqualTo(0);
    }

    @SneakyThrows
    private void takeAndVerifyReceiver() {
        Tuple3<String, String, String> request = getData(receiverServer.takeRequest(5, TimeUnit.SECONDS), String.class);
        assertThat(request._1).endsWith(AKTOR_ID + AUTOMATISK_QUERYPARAM);
        assertThat(request._2).isEqualTo("POST");
        assertThat(request._3).isNotBlank();
    }

    @SneakyThrows
    private void takeAndVerifyDialogReceiver() {
        Tuple3<String, String, String> request = getData(dialogreceiverServer.takeRequest(5, TimeUnit.SECONDS), String.class);
        assertThat(request._1).endsWith(AKTOR_ID);
        assertThat(request._2).isEqualTo("POST");
        assertThat(request._3).isNotBlank();
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
                String aktivitetDTO = "{id: "+ String.format("000%d", id.incrementAndGet()) + "}";

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
                String nyHenvendelseDTO = "{dialogId: "+ String.format("000%d", id.incrementAndGet()) + "}";


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

        String response = "{tittel: \"tittel\"}";
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
    }*/

}
