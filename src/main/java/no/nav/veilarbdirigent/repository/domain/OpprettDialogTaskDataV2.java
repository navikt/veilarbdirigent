package no.nav.veilarbdirigent.repository.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.common.types.identer.AktorId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpprettDialogTaskDataV2 {
    AktorId aktorId;
    String dialogName;
}
