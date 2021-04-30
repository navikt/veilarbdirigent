package no.nav.veilarbdirigent.config;

import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClientImpl;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClient;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClientImpl;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClientImpl;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ClientConfig {

    @Bean
    public VeilarbaktivitetClient veilarbaktivitetClient(SystemUserTokenProvider tokenProvider) {
        String url = UrlUtils.createServiceUrl("veilarbaktivitet", true);
        return new VeilarbaktivitetClientImpl(url, tokenProvider::getSystemUserToken);
    }

    @Bean
    public VeilarbdialogClient veilarbdialogClient(SystemUserTokenProvider tokenProvider) {
        String url = UrlUtils.createServiceUrl("veilarbdialog", true);
        return new VeilarbdialogClientImpl(url, tokenProvider::getSystemUserToken);
    }

    @Bean
    public VeilarbmalverkClient veilarbmalverkClient(SystemUserTokenProvider tokenProvider) {
        String url = UrlUtils.createServiceUrl("veilarbmalverk", true);
        return new VeilarbmalverkClientImpl(url, tokenProvider::getSystemUserToken);
    }

    @Bean
    public VeilarbregistreringClient veilarbregistreringClient(SystemUserTokenProvider tokenProvider) {
        String url = UrlUtils.createServiceUrl("veilarbregistrering", true);
        return new VeilarbregistreringClientImpl(url, tokenProvider::getSystemUserToken);
    }

}
