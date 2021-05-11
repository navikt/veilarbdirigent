package no.nav.veilarbdirigent.client.veilarboppfolging.domain;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class Oppfolgingsperiode {
    UUID uuid;
    String aktorId;
    ZonedDateTime startDato;
    ZonedDateTime sluttDato;
}
