package no.nav.fo.veilarbdirigent.config;

import net.javacrumbs.shedlock.core.*;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
public class DAOConfig {

    @Bean
    public LockingTaskExecutor lockProvider(DataSource dataSource) {
        return new DefaultLockingTaskExecutor(localMutex(new JdbcLockProvider(dataSource)));
    }

    @Bean
    public TaskDAO taskDAO(DataSource ds, JdbcTemplate jdbc) {
        return new TaskDAO(ds, jdbc);
    }

    static LockProvider localMutex(LockProvider original) {
        return new LockProvider() {
            private final ReentrantLock locallock = new ReentrantLock();

            @Override
            public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
                if (locallock.tryLock()) {
                    return localMutexLock(original.lock(lockConfiguration));
                } else {
                    return Optional.empty();
                }
            }

            private Optional<SimpleLock> localMutexLock(Optional<SimpleLock> maybeLock) {
                return maybeLock.map((lock) -> (SimpleLock) () -> {
                    lock.unlock();
                    locallock.unlock();
                });
            }
        };
    }
}
