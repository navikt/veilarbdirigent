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
        return new DefaultLockingTaskExecutor(new LocalMutexLockProvider(new JdbcLockProvider(dataSource)));
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
                    Optional<SimpleLock> lock = original.lock(lockConfiguration);
                    if (!lock.isPresent()) {
                        locallock.unlock();
                        return lock;
                    }

                    return localMutexLock(lock);
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

    class LocalMutexLockProvider implements LockProvider {
        private final LockProvider remoteLock;
        private final ReentrantLock locallock = new ReentrantLock();

        public LocalMutexLockProvider(LockProvider remoteLock) {
            this.remoteLock = remoteLock;
        }

        @Override
        public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
            if (locallock.tryLock()) {
                return Optional.of(new LocalMutexSimpleLock(locallock, remoteLock.lock(lockConfiguration)));
            } else {
                return Optional.empty();
            }
        }
    }

    class LocalMutexSimpleLock implements SimpleLock {
        private final ReentrantLock locallock;
        private final Optional<SimpleLock> remotelock;
        public LocalMutexSimpleLock(ReentrantLock locallock, Optional<SimpleLock> remotelock) {
            this.locallock = locallock;
            this.remotelock = remotelock;
        }


        @Override
        public void unlock() {
            remotelock.ifPresent((lock) -> lock.unlock());
            locallock.unlock();
        }
    }
}
