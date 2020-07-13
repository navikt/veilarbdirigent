package no.nav.veilarbdirigent.output;

import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.veilarbdirigent.output.services.MalverkService;
import no.nav.veilarbdirigent.output.services.VeilarbaktivitetService;
import no.nav.veilarbdirigent.output.services.VeilarbdialogService;
import no.nav.veilarbdirigent.output.services.VeilarbregisteringService;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutputConfig {

    @Bean
    public VeilarbaktivitetService veilarbaktivitetService(OkHttpClient client) {
        return new VeilarbaktivitetService(client);
    }

    @Bean
    public VeilarbdialogService veilarbdialogService(OkHttpClient client) {
        return new VeilarbdialogService(client);
    }

    @Bean
    public MalverkService malverkService(OkHttpClient client) {
        return new MalverkService(client);
    }

    @Bean
    public VeilarbregisteringService veilarbregisteringService(OkHttpClient client, AktorregisterClient aktorService) {
        return new VeilarbregisteringService(client, aktorService);
    }
}
