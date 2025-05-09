package no.nav.veilarbdirigent.config;

import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.token_client.client.MachineToMachineTokenClient;
import no.nav.veilarbdirigent.client.arbeidssoekerregisteret.ArbeidssoekerregisterClient;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClient;
import no.nav.veilarbdirigent.client.veilarbaktivitet.VeilarbaktivitetClientImpl;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Value("${app.env.veilarbaktivitetUrl}")
    private String veilarbaktivitetUrl;
    @Value("${app.env.veilarbaktivitetScope}")
    private String veilarbaktivitetScope;

    @Value("${app.env.veilarboppfolgingUrl}")
    private String veilarboppfolgingUrl;
    @Value("${app.env.veilarboppfolgingScope}")
    private String veilarboppfolgingScope;

    @Value("${app.env.arbeidsokerRegisterertOppslagUrl}")
    private String arbeidsokerRegisterertOppslagUrl;
    @Value("${app.env.arbeidsokerRegisterertOppslagScope}")
    private String arbeidsokerRegisterertOppslagScope;

    @Bean
    public VeilarbaktivitetClient veilarbaktivitetClient(AzureAdMachineToMachineTokenClient tokenClient) {
        return new VeilarbaktivitetClientImpl(veilarbaktivitetUrl, () -> tokenClient.createMachineToMachineToken(veilarbaktivitetScope));
    }

    @Bean
    public VeilarboppfolgingClient veilarboppfolgingClient(AzureAdMachineToMachineTokenClient tokenClient) {
        return new VeilarboppfolgingClientImpl(veilarboppfolgingUrl, () -> tokenClient.createMachineToMachineToken(veilarboppfolgingScope));
    }

    @Bean
    public ArbeidssoekerregisterClient arbeidssoekerregisterClient(MachineToMachineTokenClient tokenClient) {
        return new ArbeidssoekerregisterClient(arbeidsokerRegisterertOppslagUrl, () -> tokenClient.createMachineToMachineToken(arbeidsokerRegisterertOppslagScope));
    }
}
