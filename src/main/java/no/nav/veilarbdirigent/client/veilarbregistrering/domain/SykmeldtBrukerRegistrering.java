package no.nav.veilarbdirigent.client.veilarbregistrering.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SykmeldtBrukerRegistrering {
    LocalDateTime opprettetDato;
    Besvarelse besvarelse;
}
