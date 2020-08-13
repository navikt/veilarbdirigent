package no.nav.veilarbdirigent.config;

import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.handlers.AktivitetHandler;
import no.nav.veilarbdirigent.handlers.BusyHandler;
import no.nav.veilarbdirigent.handlers.DialogHandler;
import no.nav.veilarbdirigent.output.services.MalverkService;
import no.nav.veilarbdirigent.output.services.VeilarbaktivitetService;
import no.nav.veilarbdirigent.output.services.VeilarbdialogService;
import no.nav.veilarbdirigent.output.services.VeilarbregisteringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfig {
    @Bean
    public AktivitetHandler oppfolgingsHandler(Core core, VeilarbaktivitetService service, MalverkService malverk) {
        return new AktivitetHandler(core, service, malverk);
    }

    @Bean
    public DialogHandler dialogHandler(Core core, VeilarbregisteringService veilarbregisteringService, VeilarbdialogService service) {
        return new DialogHandler(core, veilarbregisteringService, service);
    }

    @Bean
    public BusyHandler busyHandler(Core core) {
        return new BusyHandler(core);
    }
}
