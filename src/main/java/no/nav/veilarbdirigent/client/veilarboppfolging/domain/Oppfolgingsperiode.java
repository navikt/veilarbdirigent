package no.nav.veilarbdirigent.client.veilarboppfolging.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.common.types.identer.AktorId;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Oppfolgingsperiode {
    UUID uuid;
    AktorId aktorId;
    ZonedDateTime startDato;
    ZonedDateTime sluttDato;
}
