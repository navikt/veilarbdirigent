package no.nav.fo.veilarbdirigent.config;

import no.nav.sbl.rest.RestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;

@Configuration
public class ClientTestConfig {

    @Bean
    public Client client() {
        return RestUtils.createClient();
    }
}
