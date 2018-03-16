package no.nav.fo.veilarbdirigent.handlers.activities;

import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.Value;
import no.nav.fo.veilarbdirigent.core.outgoingmessage.OutgoingMessageDefinition;
import no.nav.fo.veilarbdirigent.core.outgoingmessage.OutgoingMessageDefinitionLoader;
import no.nav.fo.veilarbdirigent.coreapi.*;
import no.nav.fo.veilarbdirigent.input.feed.OppfolgingDataFraFeed;
import no.nav.sbl.rest.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ActivityHandler implements MessageHandler, Actuator<ActivityHandler.ActivityData, String> {
    private static final String TYPE = "CV_ACTIVITY_TASK";
    private static final String OUTGOING_MESSAGE_TYPE = "cv_aktivitet";
    private static final Client RESTCLIENT = RestUtils.createClient();

    private String url = null;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<Task> handle(Message rawMessage) {
        if (rawMessage instanceof OppfolgingDataFraFeed) {
            OppfolgingDataFraFeed message = (OppfolgingDataFraFeed) rawMessage;

            ActivityData data = new ActivityData(
                    message.getAktorId(),
                    message.isSelvgaende()
            );

            return List.of(
                    Task.<ActivityData, ActivityResponse>builder()
                            .id(message.getId() + "-" + OUTGOING_MESSAGE_TYPE)
                            .type(TYPE)
                            .status(Status.PENDING)
                            .data(data)
                            .build()
            );
        }
        return List.empty();
    }

    @Override
    public Task<ActivityData, String> handle(Task<ActivityData, String> task) {
        Option<ActivityDefinition> activityDefinitions = OutgoingMessageDefinitionLoader.get(OUTGOING_MESSAGE_TYPE, ActivityDefinition.class);
        Option<OutgoingMessage> outgoingMessage = activityDefinitions.map((msg) -> msg.completeWith(task));

        Option<Task<ActivityData, String>> newTask = outgoingMessage
                .map((msg) -> {
                    Response response = RESTCLIENT.target(url)
                            .request()
                            .post(Entity.entity(outgoingMessage, MediaType.APPLICATION_JSON_TYPE));

                    boolean wasOk = response.getStatus() >= 200 && response.getStatus() < 300;

                    return task
                            .withStatus(wasOk ? Status.OK : Status.FAILED)
                            .withResult(wasOk ? response.readEntity(String.class) : null)
                            .withError(wasOk ? null : response.readEntity(String.class));
                });

        return newTask.getOrElse(task);
    }

    public static class ActivityDefinition implements OutgoingMessageDefinition<ActivityData, String> {
        public String type;
        public String tittel;
        public String hensikt;
        public String oppfolging;

        @Override
        public OutgoingMessage completeWith(Task<ActivityData, String> task) {
            return new OutgoingMessage() {
            };
        }
    }

    @Value
    public static class ActivityData {
        String aktorId;
        boolean selvgaende;
    }

    @Value
    public static class ActivityResponse {}
}
