package no.nav.veilarbdirigent.client.veilarbregistrering.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdinaerBrukerRegistrering {
    Besvarelse besvarelse;
    Profilering profilering;
    LocalDateTime opprettetDato;
}
