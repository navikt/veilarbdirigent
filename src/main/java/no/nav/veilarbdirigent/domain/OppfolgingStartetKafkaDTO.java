package no.nav.veilarbdirigent.domain;

import lombok.Value;
import no.nav.common.types.identer.AktorId;

import java.time.ZonedDateTime;

@Value
public class OppfolgingStartetKafkaDTO {
    private AktorId aktorId;
    private ZonedDateTime oppfolgingStartet;
}