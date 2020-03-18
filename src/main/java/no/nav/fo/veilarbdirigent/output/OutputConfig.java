package no.nav.fo.veilarbdirigent.output;

import no.nav.fo.veilarbdirigent.output.services.MalverkService;
import no.nav.fo.veilarbdirigent.output.services.VeilarbaktivitetService;
import no.nav.fo.veilarbdirigent.output.services.VeilarbdialogService;
import no.nav.fo.veilarbdirigent.output.services.VeilarbregisteringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutputConfig {

    @Bean
    public VeilarbaktivitetService veilarbaktivitetService() {
        return new VeilarbaktivitetService();
    }

    @Bean
    public VeilarbdialogService veilarbdialogService() {
        return new VeilarbdialogService();
    }

    @Bean
    public MalverkService malverkService() {
        return new MalverkService();
    }

    @Bean
    public VeilarbregisteringService veilarbregisteringService() {
        return new VeilarbregisteringService();
    }
}
