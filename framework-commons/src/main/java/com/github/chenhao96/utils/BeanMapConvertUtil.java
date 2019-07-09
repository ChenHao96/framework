/**
 * Copyright 2019 ChenHao96
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.chenhao96.utils;

import com.github.chenhao96.utils.mapper.Jackson2FlatMapper;

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
