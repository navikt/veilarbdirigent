package no.nav.veilarbdirigent.client.veilarbregistrering.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BrukerRegistreringWrapper {
    BrukerRegistreringType type;
    OrdinaerBrukerRegistrering ordinaerBrukerRegistrering;
    SykmeldtBrukerRegistrering sykmeldtBrukerRegistrering;
    // Either ordinaerBrukerRegistrering or sykmeldtBrukerRegistrering is null
}
