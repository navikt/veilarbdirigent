package no.nav.fo.veilarbdirigent.config;

import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.brukerdialog.security.oidc.OidcFeedOutInterceptor;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import no.nav.sbl.jdbc.Transactor;
import no.nav.sbl.rest.RestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAsync
@EnableScheduling
public class CoreConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService taskExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

    @Bean
    public Transactor transactor(PlatformTransactionManager transactionManager) {
        return new Transactor(transactionManager);
    }

    @Bean
    public Core core(TaskDAO taskDAO, LockingTaskExecutor lock, ScheduledExecutorService scheduler, Transactor transactor) {
        return new Core(taskDAO, scheduler, lock, transactor);
    }

    @Bean
    public Client client() {
        Client client = RestUtils.createClient();
        client.register(new SystemUserOidcTokenProviderFilter());
        return client;
    }

    private static class SystemUserOidcTokenProviderFilter implements ClientRequestFilter {
        private SystemUserTokenProvider systemUserTokenProvider = new SystemUserTokenProvider();

        @Override
        public void filter(ClientRequestContext clientRequestContext) throws IOException {
            clientRequestContext.getHeaders().putSingle("Authorization", "Bearer " + systemUserTokenProvider.getToken());
        }
    }

}
