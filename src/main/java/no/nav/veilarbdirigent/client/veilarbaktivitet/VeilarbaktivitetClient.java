package no.nav.veilarbdirigent.client.veilarbaktivitet;

import io.vavr.control.Try;

import java.util.UUID;

public interface VeilarbaktivitetClient {

    Try<String> lagAktivitet(String data, UUID oppfolgingsPeriodeId);

    Try<Boolean> isOppfolgingsperiodeConsumerDisabledToggle();

}
