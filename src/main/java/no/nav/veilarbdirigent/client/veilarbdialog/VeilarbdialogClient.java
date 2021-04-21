package no.nav.veilarbdirigent.client.veilarbdialog;

import io.vavr.control.Try;

public interface VeilarbdialogClient {

    Try<String> lagDialog(String aktorId, String data);

}
