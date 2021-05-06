package no.nav.veilarbdirigent.repository.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.common.types.identer.AktorId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpprettAktivitetTaskData {
    AktorId aktorId;
    String malName;
}
