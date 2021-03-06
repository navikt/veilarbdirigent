package no.nav.veilarbdirigent.feed.exception;


import no.nav.veilarbdirigent.feed.common.FeedWebhookResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class NoWebhookUrlException extends WebApplicationException{
    public NoWebhookUrlException() {
        super(
                Response
                .status(NOT_FOUND)
                .entity(new FeedWebhookResponse().setMelding("Ingen webhook-url er satt"))
                .build()
        );
    }
}
