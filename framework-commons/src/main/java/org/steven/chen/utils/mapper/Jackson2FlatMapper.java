package org.steven.chen.utils.mapper;

import java.io.IOException;
import java.util.Map;

@Deprecated
public final class Jackson2FlatMapper {

    private static final Jackson2FlatMapperK jackson2FlatMapperK = new Jackson2FlatMapperK();

    @Deprecated
    public <T> T fromFlatMapper(Map<String, Object> target, Class<T> clazz) throws IOException {
        return jackson2FlatMapperK.fromFlatMapper(target, clazz);
    }

    @Deprecated
    public Map<String, Object> toFlatMapper(Object obj) {
        return jackson2FlatMapperK.toFlatMapper(obj);
    }

    @Deprecated
    public Map<String, Object> fromFlatMapper(Map<String, Object> target) {
        return jackson2FlatMapperK.fromFlatMapper(target);
    }
}
