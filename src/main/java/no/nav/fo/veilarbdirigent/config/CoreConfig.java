package no.nav.fo.veilarbdirigent.config;

import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import no.nav.sbl.jdbc.Transactor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

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
    public Core core(
            TaskDAO taskDAO,
            LockingTaskExecutor lock,
            ScheduledExecutorService scheduler,
            Transactor transactor
    ) {

        return new Core(
                taskDAO,
                scheduler,
                lock,
                transactor
        );
    }
}
