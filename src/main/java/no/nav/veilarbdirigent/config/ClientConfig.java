package no.nav.veilarbdirigent.config;

import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.token_client.client.MachineToMachineTokenClient;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClientImpl;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClientImpl;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.EnvironmentUtils.isDevelopment;
import static no.nav.common.utils.EnvironmentUtils.isProduction;
import static no.nav.common.utils.UrlUtils.*;

@Configuration
public class ClientConfig {

    @Value("${app.env.veilarbaktivitetUrl}")
    private String veilarbaktivitetUrl;

    @Value("${app.env.veilarbaktivitetScope}")
    private String veilarbaktivitetScope;

    private String devFss = "dev-fss";
    private String prodFss = "prod-fss";
    private String scope(String appName, String namespace, String cluster) {
        return String.format("api://%s.%s.%s/.default", cluster, namespace, appName);
    }
    private boolean isDev = isDevelopment().orElse(false);
    private String veilarboppfolgingapiScope = scope("veilarboppfolging", "pto", isDev ? devFss : prodFss);

    @Bean
    public VeilarbaktivitetClient veilarbaktivitetClient(AzureAdMachineToMachineTokenClient tokenClient) {
        return new VeilarbaktivitetClientImpl(veilarbaktivitetUrl, () -> tokenClient.createMachineToMachineToken(veilarbaktivitetScope));
    }

    @Bean
    public VeilarboppfolgingClient veilarboppfolgingClient(AzureAdMachineToMachineTokenClient tokenClient) {
        String url = UrlUtils.createServiceUrl("veilarboppfolging", "pto", true);
        return new VeilarboppfolgingClientImpl(url, () -> tokenClient.createMachineToMachineToken(veilarboppfolgingapiScope));
    }

    @Bean
    public VeilarbmalverkClient veilarbmalverkClient() {
        String url = isDevelopment().orElse(false)
                ? createAppAdeoPreprodIngressUrl("veilarbmalverk", getEnvironment())
                : createAppAdeoProdIngressUrl("veilarbmalverk");
        return new VeilarbmalverkClientImpl(url);
    }

    @Bean
    public VeilarbregistreringClient veilarbregistreringClient(AzureAdMachineToMachineTokenClient tokenClient) {
        var appName = "veilarbregistrering";
        String url = isDevelopment().orElse(false)
                ? joinPaths(createDevInternalIngressUrl(appName), appName)
                : joinPaths(createProdInternalIngressUrl(appName), appName);
        var cluster = isDevelopment().orElse(false) ? "dev-gcp" : "prod-gcp";
        return new VeilarbregistreringClientImpl(
                url,
                () -> tokenClient.createMachineToMachineToken(scope("veilarbregistrering", "paw", cluster))
        );
    }

    @Bean
    public ArbeidssoekerregisterClient arbeidssoekerregisterClient(MachineToMachineTokenClient tokenClient) {
        var url = isDevelopment().orElse(false)
                ? "https://oppslag-arbeidssoekerregisteret.intern.dev.nav.no"
                : "https://oppslag-arbeidssoekerregisteret.intern.nav.no";
        String tokenScope = String.format("api://%s-gcp.paw.paw-arbeidssoekerregisteret-api-oppslag/.default",
                isProduction().orElse(false) ? "prod" : "dev");
        return new ArbeidssoekerregisterClient(url, () -> tokenClient.createMachineToMachineToken(tokenScope));
    }

    private static String getEnvironment() {
        return EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME");
    }

}
