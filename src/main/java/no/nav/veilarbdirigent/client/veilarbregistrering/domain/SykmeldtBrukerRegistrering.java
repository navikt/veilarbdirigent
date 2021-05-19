package no.nav.veilarbdirigent.client.veilarbregistrering.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SykmeldtBrukerRegistrering {
    LocalDateTime opprettetDato;
    Besvarelse besvarelse;
}
