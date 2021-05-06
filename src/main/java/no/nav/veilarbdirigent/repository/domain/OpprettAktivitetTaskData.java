package no.nav.veilarbdirigent.repository.domain;

import lombok.Data;
import no.nav.common.types.identer.AktorId;

@Data
public class OpprettAktivitetTaskData {
    AktorId aktorId;
    String malName;
}
