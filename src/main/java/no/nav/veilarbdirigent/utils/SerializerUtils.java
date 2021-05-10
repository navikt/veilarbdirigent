package no.nav.veilarbdirigent.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Function;

public class SerializerUtils {
    public final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new ParameterNamesModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JSR310Module());
    }

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
