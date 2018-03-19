package no.nav.fo.veilarbdirigent.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Java6Assertions.assertThat;

class TimeUtilsTest {
    static LocalDateTime now = LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0);

    @Test
    void plus_hours() {
        LocalDateTime future = TimeUtils.relativeTime(now, "12t");
        assertThat(future).isEqualByComparingTo(LocalDateTime.of(1970, 1, 1, 12, 0));
    }

    @Test
    void plus_days() {
        LocalDateTime future = TimeUtils.relativeTime(now, "5d");
        assertThat(future).isEqualByComparingTo(LocalDateTime.of(1970, 1, 6, 0, 0));
    }

    @Test
    void plus_weeks() {
        LocalDateTime future = TimeUtils.relativeTime(now, "5u");
        assertThat(future).isEqualByComparingTo(LocalDateTime.of(1970, 2, 5, 0, 0));
    }

    @Test
    void plus_months() {
        LocalDateTime future = TimeUtils.relativeTime(now, "2m");
        assertThat(future).isEqualByComparingTo(LocalDateTime.of(1970, 3, 1, 0, 0));
    }

    @Test
    void plus_combination() {
        LocalDateTime future = TimeUtils.relativeTime(now, "2m5u5d12t");
        LocalDateTime future2 = TimeUtils.relativeTime(now, "2m 5u 5d 12t");

        LocalDateTime expected = LocalDateTime.of(1970, 4, 10, 12, 0);
        assertThat(future).isEqualByComparingTo(expected);
        assertThat(future2).isEqualByComparingTo(expected);
    }
}