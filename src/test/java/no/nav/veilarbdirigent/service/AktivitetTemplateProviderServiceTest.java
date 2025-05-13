package no.nav.veilarbdirigent.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AktivitetTemplateProviderServiceTest {

    @Test
    void should_replace_fradato_and_tildato() {
        AktivitetTemplateProviderService aktivitetTemplateProviderService = new AktivitetTemplateProviderService();
        String mal = aktivitetTemplateProviderService.getCvJobbprofilAktivitetMal();
        assertNotNull(mal);
        assertFalse(mal.contains("{{FRADATO}}"));
        assertFalse(mal.contains("{{TILDATO}}"));
    }
}