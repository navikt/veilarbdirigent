package no.nav.veilarbdirigent.utils;

import java.time.LocalDateTime;

public class TimeUtils {
    public static LocalDateTime exponentialBackoff(int attempts, LocalDateTime now) {
        long secondsToWait = (long) Math.pow(2, attempts);
        return now.plusSeconds(secondsToWait);
    }
}
