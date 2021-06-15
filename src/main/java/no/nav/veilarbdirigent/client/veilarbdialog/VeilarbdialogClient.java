package no.nav.veilarbdirigent.client.veilarbdialog;

import io.vavr.control.Try;
import no.nav.common.types.identer.AktorId;

public interface VeilarbdialogClient {

    Try<String> lagDialog(AktorId aktorId, String data);

}
