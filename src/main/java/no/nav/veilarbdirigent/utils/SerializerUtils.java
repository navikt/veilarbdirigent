package no.nav.veilarbdirigent.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Function;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class SerializerUtils {
    public final static ObjectMapper mapper = JsonMapper.builder()
            .configure(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .build();

    public static LocalDateTime deserialize(Timestamp timestamp) {
        return nullAllowed(timestamp, Timestamp::toLocalDateTime);
    }

    private static <S, T> T nullAllowed(S s, Function<S, T> mapper) {
        if (s == null) {
            return null;
        }
        return mapper.apply(s);
    }
}
