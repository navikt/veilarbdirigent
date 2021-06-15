package no.nav.veilarbdirigent.repository.domain;

import lombok.Data;
import no.nav.veilarbdirigent.feed.OppfolgingDataFraFeed;

@Data
public class OpprettDialogTaskDataV1 {

    OppfolgingDataFraFeed feedelement;

    String meldingsName;

}
