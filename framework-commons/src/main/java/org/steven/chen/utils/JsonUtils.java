package org.steven.chen.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper stringMapper = new ObjectMapper();

    public static String object2Json(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T jsonStr2Object(String jsonStr, Class<T> clazz) throws IOException {
        return mapper.readValue(jsonStr, clazz);
    }

    public static JsonNode jsonStr2JsonNode(String jsonStr) throws IOException {
        return mapper.readTree(jsonStr);
    }

    public static <T> T jsonStr2TypeReference(String jsonStr, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(jsonStr, valueTypeRef);
    }

    public static String object2JsonUseStringValue(Object obj) throws JsonProcessingException {
        return stringMapper.writeValueAsString(obj);
    }

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        stringMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        stringMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        stringMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        stringMapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
    }
}
