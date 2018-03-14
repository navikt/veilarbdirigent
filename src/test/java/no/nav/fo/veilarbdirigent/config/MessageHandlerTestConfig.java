package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.coreapi.MessageHandler;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

public class MessageHandlerTestConfig {
    @Bean
    public MessageHandler messageHandler() {
        return mock(MessageHandler.class);
    }
}
