package no.nav.veilarbdirigent.client.veilarboppfolging.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.common.types.identer.AktorId;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Oppfolgingsperiode {
    public UUID uuid;
    public String aktorId;
    public String veileder;
    public ZonedDateTime startDato;
    public ZonedDateTime sluttDato;
    public String begrunnelse;
    public List<KvpPeriodeDTO> kvpPerioder;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class KvpPeriodeDTO {
    ZonedDateTime opprettetDato;
    ZonedDateTime avsluttetDato;
}
