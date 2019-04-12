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

package org.steven.chen.utils.mapper;

import org.steven.chen.utils.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class DefaultObject2FlatMapper<D> implements HashMapper<D> {

    protected static final String PARTING = ".";
    protected static final String ARRAY_PREFIX = "[";
    protected static final String ARRAY_SUFFIX = "]";

    @Override
    public <T> T fromFlatMapper(Map<String, Object> target, Class<T> clazz) {
        Map<String, Object> cacheMap = new LinkedHashMap<>();
        try {
            mapFlatten(target, cacheMap);
            return JsonUtils.jsonStr2Object(JsonUtils.object2Json(cacheMap), clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mapFlatten(Map<String, Object> target, Map<String, Object> cacheMap) throws Exception {
        if (target == null) return;
        doUnFlatten("", cacheMap, target);
    }

    @SuppressWarnings("unchecked")
    private void doUnFlatten(String propertyPrefix, Map<String, Object> cacheMap, Map<String, Object> target) throws Exception {
        Set<Map.Entry<String, Object>> entries = target.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith(propertyPrefix)) {
                key = key.substring(0, propertyPrefix.length());
                if (key.contains(PARTING)) {
                    key = key.substring(0, key.indexOf(PARTING) + 1);
                    Map<String, Object> childMap = value == null ? new LinkedHashMap<>() : (Map<String, Object>) value;
                    doUnFlatten(key, childMap, target);
                } else if (key.contains(ARRAY_PREFIX)) {

                } else {
                    cacheMap.put(key, value);
                }
            }
        }
    }
}
