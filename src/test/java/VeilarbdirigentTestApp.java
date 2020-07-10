import no.nav.veilarbdirigent.config.ApplicationTestConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;


@EnableAutoConfiguration
@Import(ApplicationTestConfig.class)
public class VeilarbdirigentTestApp {

    public static void main(String[] args) throws Exception {
        SpringApplication application = new SpringApplication(VeilarbdirigentTestApp.class);
        application.setAdditionalProfiles("local");
        application.run(args);
    }
}
