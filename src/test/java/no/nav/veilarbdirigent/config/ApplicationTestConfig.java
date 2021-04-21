package no.nav.veilarbdirigent.config;

import no.nav.common.utils.Credentials;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationTestConfig {

    @Bean
    public Credentials serviceUserCredentials() {
        return new Credentials("username", "password");
    }

}
