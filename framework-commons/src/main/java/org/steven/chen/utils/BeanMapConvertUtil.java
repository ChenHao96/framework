package org.steven.chen.utils;

import org.steven.chen.utils.mapper.Jackson2FlatMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BeanMapConvertUtil {

    private static final Jackson2FlatMapper jackson2FlatMapper = new Jackson2FlatMapper();

    public static Map<String, Object> bean2Map(Object obj) {
        return jackson2FlatMapper.toFlatMapper(obj);
    }

    public static Map<String, String> bean2StringMap(Object obj) {
        Map<String, Object> map = bean2Map(obj);
        if (map == null || map.size() == 0) return null;
        Map<String, String> result = new HashMap<>(map.size());
        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            result.put(entry.getKey(), entry.getValue().toString());
        }
        return result;
    }

    public static <T> T map2Bean(Map<String, ?> map, Class<T> clazz) {
        try {
            return jackson2FlatMapper.fromFlatMapper(map, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
