package no.nav.fo.veilarbdirigent.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vavr.control.Option;
import no.nav.fo.veilarbdirigent.core.outgoingmessage.OutgoingMessageDefinition;
import no.nav.fo.veilarbdirigent.core.outgoingmessage.OutgoingMessageDefinitionLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;


class OutgoingMessageDefinitionLoaderTest {
    @Test
    void should_read_file_into_class() {
        Option<OutgoingMessageDefinition<AktivitetTestData>> cvAktivitet = OutgoingMessageDefinitionLoader.get("test_cv_aktivitet", AktivitetTestData.class);

        assertThat(cvAktivitet.isSingleValued()).isTrue();
        assertThat(cvAktivitet.get().getData().type).isEqualTo("TEST");
        assertThat(cvAktivitet.get().getData().tittel).isEqualTo("Tittel");
        assertThat(cvAktivitet.get().getData().hensikt).isEqualTo("Hensikt");
    }

    @Test
    void should_give_option_none_if_it_doesnt_exist() {
        Option<OutgoingMessageDefinition<AktivitetTestData>> cvAktivitet = OutgoingMessageDefinitionLoader.get("fake-task", AktivitetTestData.class);

        assertThat(cvAktivitet.isEmpty()).isTrue();
    }

    @Test
    void should_give_option_none_if_marshalling_fails() {
        Option<OutgoingMessageDefinition<String>> cvAktivitet = OutgoingMessageDefinitionLoader.get("test_cv_aktivitet", String.class);

        assertThat(cvAktivitet.isEmpty()).isTrue();
    }

    // NB, be careful with using lombok on POJOs meant for jackson
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AktivitetTestData {
        public String type;
        public String tittel;
        public String hensikt;
    }
}