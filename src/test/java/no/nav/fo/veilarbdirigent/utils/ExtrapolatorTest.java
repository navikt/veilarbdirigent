package no.nav.fo.veilarbdirigent.utils;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ExtrapolatorTest {
    private final LocalDateTime now = LocalDateTime.of(2018, 3, 20, 10, 0);
    private final ZoneOffset zone = ZoneOffset.systemDefault().getRules().getOffset(now);
    private final Clock clock = Clock.fixed(now.toInstant(zone), zone);

    private final Extrapolator extrapolator = new Extrapolator(clock);

    @Test
    public void should_extrapolate_multiple() {
        assertThat(extrapolator.extrapolate("DATA {now+12t} BETWEEN { now + 1d } VARS"))
                .isEqualTo("DATA 2018-03-20T22:00:00.000 BETWEEN 2018-03-21T10:00:00.000 VARS");
    }

    @Test
    public void should_extrapolate_now() {
        assertThat(extrapolator.extrapolate(prep("{now}"))).isEqualTo(prep("2018-03-20T10:00:00.000"));
        assertThat(extrapolator.extrapolate(prep("{now }"))).isEqualTo(prep("2018-03-20T10:00:00.000"));
        assertThat(extrapolator.extrapolate(prep("{ now}"))).isEqualTo(prep("2018-03-20T10:00:00.000"));
        assertThat(extrapolator.extrapolate(prep("{ now }"))).isEqualTo(prep("2018-03-20T10:00:00.000"));
    }

    @Test
    void should_extrapolate_and_calculate_dates_in_future() {
        assertThat(extrapolator.extrapolate(prep("{now + 7d}"))).isEqualTo(prep("2018-03-27T10:00:00.000"));
        assertThat(extrapolator.extrapolate(prep("{now + 7d }"))).isEqualTo(prep("2018-03-27T10:00:00.000"));
        assertThat(extrapolator.extrapolate(prep("{ now +7d}"))).isEqualTo(prep("2018-03-27T10:00:00.000"));
        assertThat(extrapolator.extrapolate(prep("{ now+7d }"))).isEqualTo(prep("2018-03-27T10:00:00.000"));
    }

    @Test
    void should_support_environment() {
        System.setProperty("miljo", "");
        assertThat(extrapolator.extrapolate(prep("{miljo}"))).isEqualTo(prep(""));
        assertThat(extrapolator.extrapolate(prep("https://tjenester{miljo}.nav.no/test")))
                .isEqualTo(prep("https://tjenester.nav.no/test"));

        System.setProperty("miljo", "t6");
        assertThat(extrapolator.extrapolate(prep("{miljo}"))).isEqualTo(prep("-t6"));
        assertThat(extrapolator.extrapolate(prep("https://tjenester{miljo}.nav.no/test")))
                .isEqualTo(prep("https://tjenester-t6.nav.no/test"));
    }

    private static String prep(String s) {
        return String.format("random data%s more random data", s);
    }
}