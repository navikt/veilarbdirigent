package no.nav.veilarbdirigent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {

    private String openAmDiscoveryUrl;

    private String openAmRedirectUrl;

    private String openAmUsername;

    private String openAmPassword;

    private String stsDiscoveryUrl;

    private String aktorregisterUrl;

}
