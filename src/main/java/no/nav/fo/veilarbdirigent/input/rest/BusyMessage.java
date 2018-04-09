package no.nav.fo.veilarbdirigent.input.rest;

import lombok.Value;
import no.nav.fo.veilarbdirigent.core.api.Message;

import java.util.UUID;

@Value
public class BusyMessage implements Message {
    UUID id = UUID.randomUUID();
}
