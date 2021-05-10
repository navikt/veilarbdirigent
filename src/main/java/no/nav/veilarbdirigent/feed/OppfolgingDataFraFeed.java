package no.nav.veilarbdirigent.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OppfolgingDataFraFeed implements Comparable<OppfolgingDataFraFeed> {
    long id;
    String aktorId;
    String foreslattInnsatsgruppe;
    Date opprettet;
    String sykmeldtBrukerType;

    @Override
    public int compareTo(OppfolgingDataFraFeed o) {
        return Long.compare(id, o.id);
    }
}
