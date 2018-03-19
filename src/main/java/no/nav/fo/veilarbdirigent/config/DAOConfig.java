package no.nav.fo.veilarbdirigent.config;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import no.nav.fo.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DAOConfig {

    @Bean
    public LockingTaskExecutor lockProvider(DataSource dataSource) {
        return new DefaultLockingTaskExecutor(new JdbcLockProvider(dataSource));
    }

    @Bean
    public TaskDAO taskDAO(JdbcTemplate jdbc) {
        return new TaskDAO(jdbc);
    }
}
