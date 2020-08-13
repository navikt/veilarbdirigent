package no.nav.veilarbdirigent.input.rest;

import lombok.Value;
import no.nav.veilarbdirigent.core.api.Message;

import java.util.UUID;

@Value
public class BusyMessage implements Message {
    UUID id = UUID.randomUUID();
}
