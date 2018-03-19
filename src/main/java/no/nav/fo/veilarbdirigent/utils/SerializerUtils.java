package no.nav.fo.veilarbdirigent.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import lombok.SneakyThrows;

public class SerializerUtils {
    public final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new ParameterNamesModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JSR310Module());
    }

    @SneakyThrows
    public static String serialize(Object object) {
        return mapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static TypedField deserialize(String data) {
        if (data == null) {
            return null;
        }

        return mapper.readValue(data, TypedField.class);
    }
}
