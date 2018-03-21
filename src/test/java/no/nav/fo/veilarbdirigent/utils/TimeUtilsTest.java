package no.nav.fo.veilarbdirigent.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Java6Assertions.assertThat;

class TimeUtilsTest {
    static LocalDateTime now = LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0);

    @Test
    void plus_hours() {
        LocalDateTime future = TimeUtils.relativeTime(now, "12t");

        // 1970.1.1 is a holyday, hence +1d
        assertThat(future).isEqualByComparingTo(LocalDateTime.of(1970, 1, 2, 12, 0));
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

        // 1970.3.1 is a sunday, hence +1d
        assertThat(future).isEqualByComparingTo(LocalDateTime.of(1970, 3, 2, 0, 0));
    }

    @Test
    void should_return_working_day() {
        // now: 1970.1.1 thursday
        // +2d: 1970.1.3 saturday is not a working day, fastforward to next monday 1970.1.5
        LocalDateTime future = TimeUtils.relativeTime(now, "2d");
        assertThat(future).isEqualByComparingTo(LocalDateTime.of(1970, 1, 5, 0, 0));

        // now: 1970.1.1 thursday
        // +3d: 1970.1.4 saturday is not a working day, fastforward to next monday 1970.1.5
        LocalDateTime future2 = TimeUtils.relativeTime(now, "2d");
        assertThat(future2).isEqualByComparingTo(LocalDateTime.of(1970, 1, 5, 0, 0));

        // now: 2018.3.28 wednesday
        // +1d: 2018.3.29 thursday is not a working day (easter), fastforward to 2018.4.3
        LocalDateTime easter = LocalDateTime.of(2018, 3, 28, 0, 0, 0, 0);
        LocalDateTime future3 = TimeUtils.relativeTime(easter, "1d");
        assertThat(future3).isEqualByComparingTo(LocalDateTime.of(2018, 4, 3, 0, 0));
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