package no.nav.fo.veilarbdirigent.config;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.fo.veilarbdirigent.core.Core;
import no.nav.fo.veilarbdirigent.coreapi.Actuator;
import no.nav.fo.veilarbdirigent.coreapi.MessageHandler;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@EnableScheduling
public class CoreConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        return threadPoolTaskScheduler;
    }

    @Bean
    public Core core(
            java.util.List<MessageHandler> handlers,
            java.util.List<Actuator> actuators,
            LockingTaskExecutor lock,
            ThreadPoolTaskScheduler taskScheduler,
            TaskDAO taskDAO
    ) {

        return new Core(
                List.ofAll(handlers),
                List.ofAll(actuators),
                lock,
                taskScheduler,
                taskDAO
        );
    }
}
