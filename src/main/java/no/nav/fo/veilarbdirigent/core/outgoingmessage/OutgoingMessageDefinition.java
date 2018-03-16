package no.nav.fo.veilarbdirigent.core.outgoingmessage;

import no.nav.fo.veilarbdirigent.coreapi.OutgoingMessage;
import no.nav.fo.veilarbdirigent.coreapi.Task;

public interface OutgoingMessageDefinition<DATA, RESULT> {
    public OutgoingMessage completeWith(Task<DATA, RESULT> task);
}
