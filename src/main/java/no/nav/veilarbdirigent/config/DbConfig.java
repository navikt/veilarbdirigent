package no.nav.veilarbdirigent.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DbConfig.DatasourceProperties.class)
@RequiredArgsConstructor
@Slf4j
public class DbConfig {

    private final DatasourceProperties datasourceProperties;


    @Bean
    public DataSource dataSource() {
        var config = new HikariConfig();
        // Url with credentials
        config.setJdbcUrl(datasourceProperties.url);
//        config.setUsername(datasourceProperties.username);
//        config.setPassword(datasourceProperties.password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        var dataSource = new HikariDataSource(config);
        migrateDb(dataSource);
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    static public void migrateDb(DataSource dataSource) {
        var flyway = Flyway
                .configure()
                .baselineVersion(MigrationVersion.fromVersion("2"))
                .baselineOnMigrate(true)
                .table("schema_version")
                .dataSource(dataSource)
                .load();
        flyway.baseline();
        flyway.migrate();
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "app.datasource")
    public static class DatasourceProperties {
        String url;
        String username;
        String password;
    }

}
