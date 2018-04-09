package no.nav.fo.veilarbdirigent.output;

import no.nav.fo.veilarbdirigent.output.veilarbaktivitet.VeilarbaktivitetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutputConfig {

    @Bean
    public VeilarbaktivitetService veilarbaktivitetService() {
        return new VeilarbaktivitetService();
    }
}
