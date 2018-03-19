package no.nav.fo.veilarbdirigent.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;


class PredefinedDataLoaderTest {
    @Test
    void should_read_file_into_class() {
        Option<AktivitetTestData> cvAktivitet = PredefinedDataLoader.get("test_cv_aktivitet", AktivitetTestData.class);

        assertThat(cvAktivitet.isSingleValued()).isTrue();
        assertThat(cvAktivitet.get().type).isEqualTo("TEST");
        assertThat(cvAktivitet.get().tittel).isEqualTo("Tittel");
        assertThat(cvAktivitet.get().hensikt).isEqualTo("Hensikt");
    }

    @Test
    void should_give_option_none_if_it_doesnt_exist() {
        Option<AktivitetTestData> cvAktivitet = PredefinedDataLoader.get("fake-task", AktivitetTestData.class);

        assertThat(cvAktivitet.isEmpty()).isTrue();
    }

    @Test
    void should_give_option_none_if_marshalling_fails() {
        Option<String> cvAktivitet = PredefinedDataLoader.get("test_cv_aktivitet", String.class);

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