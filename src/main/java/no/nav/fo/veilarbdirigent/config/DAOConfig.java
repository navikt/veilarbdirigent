package no.nav.fo.veilarbdirigent.config;

import no.nav.fo.veilarbdirigent.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DAOConfig {

    @Bean
    public TaskDAO taskDAO(DataSource ds, JdbcTemplate jdbc) {
        return new TaskDAO(ds, jdbc);
    }
}
