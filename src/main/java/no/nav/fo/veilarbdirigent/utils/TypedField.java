package no.nav.fo.veilarbdirigent.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Map;

@JsonSerialize(using = TypedField.Serializer.class)
@JsonDeserialize(using = TypedField.Deserializer.class)
public class TypedField<ELEMENT> {
    private static final TypeReference<Map<String, String>> SERIALIZED_TYPE = new TypeReference<Map<String, String>>() {
    };

    public final ELEMENT element;

    public TypedField(ELEMENT element) {
        this.element = element;
    }

    public static class Deserializer extends StdDeserializer<TypedField> {
        public Deserializer() {
            this(null);
        }

        protected Deserializer(Class<?> vc) {
            super(vc);
        }

        @SuppressWarnings("unchecked")
        @Override
        public TypedField deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            Map<String, String> obj = jp.readValueAs(SERIALIZED_TYPE);
            try {
                Class<?> aClass = Class.forName(obj.getOrDefault("class", "java.lang.String"));
                String elementString = obj.get("element");

                Object element = SerializerUtils.mapper.readValue(elementString, aClass);
                return new TypedField(element);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Serializer extends StdSerializer<TypedField> {
        public Serializer() {
            this(null);
        }

        protected Serializer(Class<TypedField> t) {
            super(t);
        }

        @Override
        public void serialize(TypedField typedField, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            Object element = typedField.element;
            Class<?> elementClass = typedField.element.getClass();

            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("class", elementClass);
            jsonGenerator.writeObjectField("element", SerializerUtils.mapper.writeValueAsString(element));
            jsonGenerator.writeEndObject();
        }
    }
}