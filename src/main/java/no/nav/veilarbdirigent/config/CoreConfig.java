package no.nav.veilarbdirigent.config;

import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.veilarbdirigent.core.Core;
import no.nav.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
    public Core core(TaskDAO taskDAO, LeaderElectionClient leaderElectionClient, ScheduledExecutorService scheduler, Transactor transactor, MetricsClient metricsClient) {
        return new Core(taskDAO, scheduler, leaderElectionClient, transactor, metricsClient);
    }
}
