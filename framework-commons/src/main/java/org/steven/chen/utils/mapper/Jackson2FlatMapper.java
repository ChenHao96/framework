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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.steven.chen.utils.StringUtil;

import java.util.*;

public final class Jackson2FlatMapper implements HashMapper {

    private static final String PARTING = ".";
    private static final String ARRAY_PREFIX = "[";
    private static final String ARRAY_SUFFIX = "]";
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Map<String, Object> fromFlatMapper(Map<String, Object> target) {
        try {
            return mapFlatten(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromFlatMapper(Map<String, Object> target, Class<T> clazz) {
        try {
            Map<String, Object> cacheMap = fromFlatMapper(target);
            if (cacheMap == null) {
                return BeanUtils.instantiateClass(clazz);
            }
            return mapper.readValue(mapper.writeValueAsString(cacheMap), clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> toFlatMapper(Object object) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        try {
            JsonNode source;
            if (object instanceof JsonNode) {
                source = (JsonNode) object;
            } else {
                source = mapper.valueToTree(object);
            }
            doFlatten("", source, resultMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultMap;
    }

    private void doFlatten(String propertyPrefix, JsonNode source, Map<String, Object> resultMap) throws Exception {
        if (source.isArray()) {
            int index = 0;
            Iterator<JsonNode> arrays = source.elements();
            while (arrays.hasNext()) {
                JsonNode array = arrays.next();
                doFlatten(propertyPrefix + ARRAY_PREFIX + index++ + ARRAY_SUFFIX, array, resultMap);
            }
        } else if (source.isObject()) {
            if (StringUtil.isNotBlank(propertyPrefix)) {
                propertyPrefix = propertyPrefix + PARTING;
            }
            Iterator<Map.Entry<String, JsonNode>> iterator = source.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                doFlatten(propertyPrefix + entry.getKey(), entry.getValue(), resultMap);
            }
        } else if (source.isNumber() || source.isTextual()) {
            resultMap.put(propertyPrefix, source.asText());
        }
    }

    private Map<String, Object> mapFlatten(Map<String, Object> target) throws Exception {
        if (target == null) return null;
        Map<String, Object> resultMap = new LinkedHashMap<>();
        doUnFlatten("", resultMap, target);
        return resultMap;
    }

    private void doUnFlatten(String propertyPrefix, Map<String, Object> cacheMap, Map<String, Object> target) throws Exception {
        Set<Map.Entry<String, Object>> entries = target.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith(propertyPrefix)) {
                key = key.substring(propertyPrefix.length());
                if (key.contains(PARTING)) {
                    processMap(propertyPrefix, cacheMap, target, key);
                    continue;
                } else if (key.contains(ARRAY_PREFIX)) {
                    processArray(cacheMap, key, value);
                    continue;
                }
                cacheMap.put(key, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processMap(String propertyPrefix, Map<String, Object> cacheMap, Map<String, Object> target, String key) throws Exception {
        key = key.substring(0, key.indexOf(PARTING));
        if (key.contains(ARRAY_PREFIX)) {
            Integer index = Integer.valueOf(key.substring(key.indexOf(ARRAY_PREFIX) + 1, key.indexOf(ARRAY_SUFFIX)));
            String tmpKey = key.substring(0, key.indexOf(ARRAY_PREFIX));
            List<Object> childList = (List<Object>) cacheMap.get(tmpKey);
            if (childList == null) {
                childList = new LinkedList<>();
                cacheMap.put(tmpKey, childList);
            }
            Map<String, Object> childMap = childList.size() <= index ? null : (Map<String, Object>) childList.get(index);
            if (childMap == null) {
                childMap = new LinkedHashMap<>();
                childList.add(index, childMap);
            }
            doUnFlatten(propertyPrefix + key + PARTING, childMap, target);
        } else {
            Map<String, Object> childMap = (Map<String, Object>) cacheMap.get(key);
            if (childMap == null) {
                childMap = new LinkedHashMap<>();
                cacheMap.put(key, childMap);
            }
            doUnFlatten(propertyPrefix + key + PARTING, childMap, target);
        }
    }

    @SuppressWarnings("unchecked")
    private void processArray(Map<String, Object> cacheMap, String key, Object value) throws Exception {
        Integer index = Integer.valueOf(key.substring(key.indexOf(ARRAY_PREFIX) + 1, key.indexOf(ARRAY_SUFFIX)));
        String tmpKey = key.substring(0, key.indexOf(ARRAY_PREFIX));
        List<Object> childList = (List<Object>) cacheMap.get(tmpKey);
        if (childList == null) {
            childList = new LinkedList<>();
            cacheMap.put(tmpKey, childList);
        }
        if (childList.size() > index) {
            if (value.equals(childList.get(index))) {
                return;
            }
        }
        childList.add(index, value);
    }
}
