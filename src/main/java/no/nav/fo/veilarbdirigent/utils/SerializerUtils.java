package no.nav.fo.veilarbdirigent.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.SneakyThrows;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.function.Function;

public class SerializerUtils {
    public final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new ParameterNamesModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JSR310Module());
    }

    @SneakyThrows
    public static String serialize(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static Date deserializeToDate(String date) {
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.parse(date), ZoneId.systemDefault());
        Calendar cal = GregorianCalendar.from(zdt);
        return cal.getTime();
    }

    public static LocalDateTime deserialize(Timestamp timestamp) {
        return nullAllowed(timestamp, Timestamp::toLocalDateTime);
    }

    @SneakyThrows
    public static TypedField deserialize(String data) {
        if (data == null) {
            return null;
        }

        return mapper.readValue(data, TypedField.class);
    }

    private static <S, T> T nullAllowed(S s, Function<S, T> mapper) {
        if (s == null) {
            return null;
        }
        return mapper.apply(s);
    }
}
