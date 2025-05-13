package no.nav.veilarbdirigent.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Service
public class AktivitetTemplateProviderService {

    public String getCvJobbprofilAktivitetMal() {
        try {
            ClassPathResource resource = new ClassPathResource("cv_jobbprofil_aktivitet.json");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            var templateString = FileCopyUtils.copyToString(reader);
            var now = ZonedDateTime.now();
            return templateString
                .replace("{{FRADATO}}", now.toString())
                .replace("{{TILDATO}}", now.plusDays(8).toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read cv_jobbprofil_aktivitet.json template", e);
        }
    }

}
