package no.nav.veilarbdirigent.repository.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class OldTask {

    @JsonAlias("class")
    String className;

    String element;

}
