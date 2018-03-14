package no.nav.fo.veilarbdirigent.config;

import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.fo.veilarbdirigent.core.*;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class CoreConfig {

    @Bean
    public CoreOut coreOut(TaskDAO taskDAO, LockingTaskExecutor lock) {
        return new CoreOutImpl(taskDAO, lock);
    }

    @Bean
    public CoreIn coreIn(CoreOut coreOut, TaskDAO taskDAO) {
        return new CoreInImpl(coreOut, taskDAO);
    }

    @Bean
    public Core core(CoreIn coreIn, CoreOut coreOut) {
        return new Core(coreIn, coreOut);
    }
}
