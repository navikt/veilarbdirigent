package no.nav.veilarbdirigent.client.veilarbmalverk;

import io.vavr.control.Try;

public interface VeilarbmalverkClient {

    Try<String> hentMal(String name);

}
