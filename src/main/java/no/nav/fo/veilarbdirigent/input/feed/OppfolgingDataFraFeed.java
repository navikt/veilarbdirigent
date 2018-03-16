package no.nav.fo.veilarbdirigent.input.feed;

import lombok.Builder;
import lombok.Value;
import no.nav.fo.veilarbdirigent.coreapi.Message;

import java.util.Date;

@Value
@Builder
public class OppfolgingDataFraFeed implements Message, Comparable<OppfolgingDataFraFeed> {
    long id;
    String aktorId;
    boolean selvgaende;
    Date opprettet;

    @Override
    public int compareTo(OppfolgingDataFraFeed o) {
        return Long.compare(id, o.id);
    }
}
