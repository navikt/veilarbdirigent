package no.nav.veilarbdirigent.config;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.handlers.AktivitetHandler;
import no.nav.veilarbdirigent.handlers.BusyHandler;
import no.nav.veilarbdirigent.handlers.DialogHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfig {
    @Bean
    public AktivitetHandler oppfolgingsHandler(Core core, VeilarbaktivitetClient veilarbaktivitetClient, VeilarbmalverkClient veilarbmalverkClient) {
        return new AktivitetHandler(core, veilarbaktivitetClient, veilarbmalverkClient);
    }

    @Bean
    public DialogHandler dialogHandler(Core core, AktorOppslagClient aktorOppslagClient, VeilarbregistreringClient veilarbregistreringClient, VeilarbdialogClient veilarbdialogClient) {
        return new DialogHandler(core, aktorOppslagClient, veilarbregistreringClient, veilarbdialogClient);
    }

    @Bean
    public BusyHandler busyHandler(Core core) {
        return new BusyHandler(core);
    }
}
