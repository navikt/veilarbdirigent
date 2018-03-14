package no.nav.fo.veilarbdirigent.config;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Message;
import no.nav.fo.veilarbdirigent.core.MessageHandler;
import no.nav.fo.veilarbdirigent.core.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageHandlerConfig {

    @Bean
    public MessageHandler messageHandler() {
        return new MessageHandler() {
            @Override
            public List<Task> handle(Message message) {
                return List.empty();
            }
        };
    }
}
