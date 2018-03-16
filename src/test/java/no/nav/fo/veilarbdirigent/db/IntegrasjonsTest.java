package no.nav.fo.veilarbdirigent.db;

import no.nav.fo.veilarbdirigent.config.ApplicationConfig;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;

public class IntegrasjonsTest extends AbstractIntegrationTest {
    private JdbcTemplate jdbc = getBean(JdbcTemplate.class);

    @BeforeAll
    @BeforeClass
    public static void setupContext() {
        DatabaseTestContext.setupInMemoryContext();
        setupContext(ApplicationConfig.class);
    }

    @AfterEach
    @BeforeEach
    public void deleteTestData() {
        jdbc.update("DELETE FROM TASK");
    }
}
