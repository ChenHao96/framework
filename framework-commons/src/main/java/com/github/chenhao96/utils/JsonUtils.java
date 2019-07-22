package com.github.chenhao96.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {

    private ObjectMapper mapper;

    private static final JsonUtils JSON_UTILS = newInstance();

    public JsonUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public static JsonUtils newInstance() {
        return newInstance(JsonInclude.Include.NON_NULL, false);
    }

    public static JsonUtils newStringInstance() {
        return newInstance(JsonInclude.Include.NON_NULL, true);
    }

    public static JsonUtils newInstance(JsonInclude.Include include, boolean stringValue) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(include);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, stringValue);
        return new JsonUtils(objectMapper);
    }

    public static String object2JsonStatic(Object obj) throws IOException {
        return JSON_UTILS.object2Json(obj);
    }

    public static <T> T jsonStr2ObjectStatic(String jsonStr, Class<T> clazz) throws IOException {
        return JSON_UTILS.jsonStr2Object(jsonStr, clazz);
    }

    public static JsonNode jsonStr2JsonNodeStatic(String jsonStr) throws IOException {
        return JSON_UTILS.jsonStr2JsonNode(jsonStr);
    }

    public static <T> T jsonStr2TypeReferenceStatic(String jsonStr, TypeReference<T> valueTypeRef) throws IOException {
        return JSON_UTILS.jsonStr2TypeReference(jsonStr, valueTypeRef);
    }

    public String object2Json(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    public <T> T jsonStr2Object(String jsonStr, Class<T> clazz) throws IOException {
        return mapper.readValue(jsonStr, clazz);
    }

    public JsonNode jsonStr2JsonNode(String jsonStr) throws IOException {
        return mapper.readTree(jsonStr);
    }

    public <T> T jsonStr2TypeReference(String jsonStr, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(jsonStr, valueTypeRef);
    }
}
