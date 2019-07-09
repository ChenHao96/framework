package com.github.chenhao96.utils.mapper;

import org.steven.chen.utils.mapper.Jackson2FlatMapperK;

import java.io.IOException;
import java.util.Map;

public final class Jackson2FlatMapper {

    private Jackson2FlatMapperK jackson2FlatMapperK = new Jackson2FlatMapperK();

    public <T> T fromFlatMapper(Map<String, ?> target, Class<T> clazz) throws IOException {
        return jackson2FlatMapperK.fromFlatMapper(target, clazz);
    }

    public Map<String, Object> toFlatMapper(Object obj) {
        return jackson2FlatMapperK.toFlatMapper(obj);
    }

    public Map<String, Object> fromFlatMapper(Map<String, ?> target) {
        return jackson2FlatMapperK.fromFlatMapper(target);
    }
}
