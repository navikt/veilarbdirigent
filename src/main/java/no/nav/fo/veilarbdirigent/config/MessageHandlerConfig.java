package no.nav.fo.veilarbdirigent.config;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.MessageHandler;
import org.springframework.context.annotation.Bean;

public class MessageHandlerConfig {

    @Bean
    // Må være her for å slippe konvertering fra java.util.List til vavr overalt
    public List<MessageHandler> all(java.util.List<MessageHandler> handlers) {
        return List.ofAll(handlers);
    }
}
