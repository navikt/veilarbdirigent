package no.nav.veilarbdirigent.config;

import no.nav.common.rest.client.RestClient;
import no.nav.common.sts.ServiceToServiceTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.sts.utils.AzureAdServiceTokenProviderBuilder;
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
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static no.nav.common.utils.EnvironmentUtils.isDevelopment;
import static no.nav.common.utils.UrlUtils.*;

@Configuration
public class ClientConfig {

    @Bean
    public VeilarbaktivitetClient veilarbaktivitetClient(SystemUserTokenProvider tokenProvider) {
        String url = isDevelopment().orElse(false)
                ? createAppAdeoPreprodIngressUrl("veilarbaktivitet", getEnvironment())
                : createAppAdeoProdIngressUrl("veilarbaktivitet");

        return new VeilarbaktivitetClientImpl(url, tokenProvider::getSystemUserToken);
    }

    @Bean
    public VeilarbdialogClient veilarbdialogClient(SystemUserTokenProvider tokenProvider) {
        String url = UrlUtils.createServiceUrl("veilarbdialog", "pto", true);
        return new VeilarbdialogClientImpl(url, tokenProvider::getSystemUserToken);
    }

    @Bean
    public VeilarboppfolgingClient veilarboppfolgingClient(SystemUserTokenProvider tokenProvider) {
        String url = UrlUtils.createServiceUrl("veilarboppfolging", "pto", true);
        return new VeilarboppfolgingClientImpl(url, tokenProvider::getSystemUserToken);
    }

    @Bean
    public VeilarbmalverkClient veilarbmalverkClient(SystemUserTokenProvider tokenProvider) {
        String url = isDevelopment().orElse(false)
                ? createAppAdeoPreprodIngressUrl("veilarbmalverk", getEnvironment())
                : createAppAdeoProdIngressUrl("veilarbmalverk");

        return new VeilarbmalverkClientImpl(url, tokenProvider::getSystemUserToken);
    }

    @Bean
    public VeilarbregistreringClient veilarbregistreringClient(ServiceToServiceTokenProvider serviceToServiceTokenProvider) {
        String url = isDevelopment().orElse(false)
                ? createDevInternalIngressUrl("veilarbregistrering")
                : createAppAdeoProdIngressUrl("veilarbregistrering");
        String cluster = isDevelopment().orElse(false) ? "dev-gcp" : "prod-fss";

        return new VeilarbregistreringClientImpl(
                url,
                () -> serviceToServiceTokenProvider.getServiceToken(
                        "veilarbregistrering",
                        "paw",
                        cluster
                )
        );
    }

    @Bean
    public ServiceToServiceTokenProvider serviceToServiceTokenProvider() {
        return AzureAdServiceTokenProviderBuilder.builder()
                .withEnvironmentDefaults()
                .build();
    }

    @Bean
    public OkHttpClient okHttpClient(SystemUserTokenProvider tokenProvider) {
        var builder = RestClient.baseClientBuilder();
        builder.addInterceptor(new SystemUserOidcTokenProviderInterceptor(tokenProvider));
        return builder.build();
    }

    private static class SystemUserOidcTokenProviderInterceptor implements Interceptor {
        private SystemUserTokenProvider systemUserTokenProvider;

        private SystemUserOidcTokenProviderInterceptor(SystemUserTokenProvider systemUserTokenProvider) {
            this.systemUserTokenProvider = systemUserTokenProvider;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request newReq = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + systemUserTokenProvider.getSystemUserToken())
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(newReq);
        }
    }

    private static String getEnvironment() {
        return EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME");
    }

}
