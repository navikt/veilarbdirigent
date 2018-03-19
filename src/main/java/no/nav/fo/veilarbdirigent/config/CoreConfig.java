package no.nav.fo.veilarbdirigent.config;

import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableAsync
@EnableScheduling
public class CoreConfig{

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        return scheduler;
    }

    @Bean
    public Core core(
            TaskDAO taskDAO,
            LockingTaskExecutor lock,
            ThreadPoolTaskScheduler scheduler,
            PlatformTransactionManager transactionManager
    ) {

        return new Core(
                taskDAO,
                scheduler,
                lock,
                transactionManager
        );
    }
}
