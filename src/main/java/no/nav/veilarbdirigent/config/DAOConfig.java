package no.nav.veilarbdirigent.config;

import no.nav.veilarbdirigent.core.dao.TaskDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DAOConfig {

    @Bean
    public TaskDAO taskDAO(JdbcTemplate jdbc) {
        return new TaskDAO(jdbc);
    }
}
