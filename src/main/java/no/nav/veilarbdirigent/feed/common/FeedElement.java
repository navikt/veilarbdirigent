package no.nav.veilarbdirigent.feed.common;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbdirigent.input.OppfolgingDataFraFeed;

@Data
@Accessors(chain = true)
public class FeedElement implements Comparable<FeedElement> {
    protected String id;
    protected OppfolgingDataFraFeed element;

    @Override
    public int compareTo(FeedElement other) {
        return element.compareTo(other.getElement());
    }

}
