package no.nav.fo.veilarbdirigent.utils;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static no.nav.fo.veilarbdirigent.utils.TimeUtils.ISO8601;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class Extrapolator {
    private final Clock clock;
    private final Pattern pattern = Pattern.compile("\\{(.+?)\\}");
    private final Map<Predicate<String>, Function<String, String>> rules = HashMap.of(
            regexPredicate(TimeUtils.pattern), this::relativeTime,
            equalsPredicate("now"), this::now,
            equalsPredicate("miljo"), this::miljo
    );

    public Extrapolator() {
        this(Clock.systemDefaultZone());
    }

    Extrapolator(Clock clock) {
        this.clock = clock;
    }

    private String relativeTime(String time) {
        return TimeUtils.relativeTime(LocalDateTime.now(clock), time).format(ISO8601);
    }

    private String now(String dontCare) {
        return LocalDateTime.now(clock).format(ISO8601);
    }

    private String miljo(String dontCare) {
        return getOptionalProperty("miljo", "environment.name")
                .map((env) -> "-"+env)
                .orElse("");
    }

    public String extrapolate(String s) {
        if (s == null || "".equals(s)) {
            return s;
        }

        int last = 0;
        StringBuilder sb = new StringBuilder();

        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            sb.append(s.substring(last, matcher.start()));

            String templatename = matcher.group(1).replaceAll("\\s", "");

            Function<String, String> transformer = rules
                    .find((rule) -> rule._1.test(templatename))
                    .map((rule) -> rule._2)
                    .getOrElse(Function.identity());

            sb.append(transformer.apply(templatename));


            last = matcher.end();
        }
        if (last != s.length()) {
            sb.append(s.substring(last, s.length()));
        }

        return sb.toString();
    }

    private static Predicate<String> regexPredicate(Pattern regex) {
        return (s) -> regex.matcher(s).matches();
    }

    private static Predicate<String> equalsPredicate(String match) {
        return match::equals;
    }
}
