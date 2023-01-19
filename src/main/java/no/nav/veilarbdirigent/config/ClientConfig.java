package no.nav.veilarbdirigent.config;

import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.utils.UrlUtils;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClientImpl;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClient;
import no.nav.veilarbdirigent.client.veilarbdialog.VeilarbdialogClientImpl;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClient;
import no.nav.veilarbdirigent.client.veilarbmalverk.VeilarbmalverkClientImpl;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.EnvironmentUtils.isDevelopment;
import static no.nav.common.utils.UrlUtils.*;

@Configuration
public class ClientConfig {

    private String devFss = "dev-fss";
    private String prodFss = "prod-fss";
    private String scope(String appName, String namespace, String cluster) {
        return String.format("api://%s.%s.%s/.default", cluster, namespace, appName);
    }
    private Boolean isDev = isDevelopment().orElse(false);
    private String veilarboppfolgingapiScope = scope("veilarboppfolging", "pto", isDev ? devFss : prodFss);
    private String veilarbdialogScope = scope("veilarbdialog", "pto", isDev ? devFss : prodFss);
    private String veilarbaktivitetScope = scope("veilarbaktivitet", "pto", isDev ? devFss : prodFss);

    @Bean
    public VeilarbaktivitetClient veilarbaktivitetClient(AzureAdMachineToMachineTokenClient tokenClient) {
        String url = isDevelopment().orElse(false)
                ? createAppAdeoPreprodIngressUrl("veilarbaktivitet", getEnvironment())
                : createAppAdeoProdIngressUrl("veilarbaktivitet");

        return new VeilarbaktivitetClientImpl(url, () -> tokenClient.createMachineToMachineToken(veilarbaktivitetScope));
    }

    @Bean
    public VeilarbdialogClient veilarbdialogClient(AzureAdMachineToMachineTokenClient tokenClient) {
        String url = UrlUtils.createServiceUrl("veilarbdialog", "pto", true);
        return new VeilarbdialogClientImpl(url, () -> tokenClient.createMachineToMachineToken(veilarbdialogScope));
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

    private static String getEnvironment() {
        return EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME");
    }

}
