package no.nav.veilarbdirigent;

import no.nav.common.utils.SslUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VeilarbdirigentApp {

    public static void main(String... args) {
        SslUtils.setupTruststore();
        SpringApplication.run(VeilarbdirigentApp.class, args);
    }

}
