package no.nav.veilarbdirigent.repository.domain;

import lombok.Data;
import no.nav.veilarbdirigent.feed.OppfolgingDataFraFeed;

@Data
public class OpprettAktivitetTaskDataV1 {

    OppfolgingDataFraFeed feedelement;

    String predefineddataName;

}
