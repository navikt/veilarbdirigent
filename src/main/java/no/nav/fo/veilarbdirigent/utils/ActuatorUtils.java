package no.nav.fo.veilarbdirigent.utils;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActuatorUtils {
    private static final String MONTHS = "m";
    private static final String WEEKS = "u";
    private static final String DAYS = "d";
    private static final String HOURS = "t";

    private static Pattern pattern = Pattern.compile("(\\d+["+MONTHS+WEEKS + DAYS + HOURS+"])");

    static LocalDateTime relativeTime(LocalDateTime now, String relative) {
        LocalDateTime time = now;
        Matcher matcher = pattern.matcher(relative);

        while (matcher.find()) {
            time = createTimeFunction(matcher.group()).apply(time);
        }

        return time;
    }

    private static Function<LocalDateTime, LocalDateTime> createTimeFunction(String time) {
        return (date) -> {
            int number = Integer.parseInt(time.substring(0, time.length() - 1), 10);
            if (time.endsWith(MONTHS)) {
                return date.plusMonths(number);
            } else if (time.endsWith(WEEKS)) {
                return date.plusWeeks(number);
            } else if (time.endsWith(DAYS)) {
                return date.plusDays(number);
            } else if (time.endsWith(HOURS)) {
                return date.plusHours(number);
            } else {
                return date;
            }
        };
    }

    protected static LocalDateTime relativeTime(String relative) {
        return relativeTime(LocalDateTime.now(), relative);
    }
}
