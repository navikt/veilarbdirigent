package no.nav.fo.veilarbdirigent.output;

import no.nav.fo.veilarbdirigent.output.veilarbaktivitet.MalverkService;
import no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbaktivitetService;
import no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbdialogService;
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
}
