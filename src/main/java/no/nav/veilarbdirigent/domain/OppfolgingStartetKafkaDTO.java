package no.nav.veilarbdirigent.domain;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class OppfolgingStartetKafkaDTO {
    private String aktorId;
    private ZonedDateTime oppfolgingStartet;
}