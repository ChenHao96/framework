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

import org.steven.chen.utils.CommonsUtil;
import org.steven.chen.utils.StringUtil;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

public class Object2FlatMapperBean extends DefaultObject2FlatMapper<Object> {

    private Set<Object> currentSource = new HashSet<>();

    @Override
    public Map<String, Object> toFlatMapper(Object object) {
        this.currentSource.add(object);
        try {
            return flattenMap(object);
        } finally {
            this.currentSource.clear();
        }
    }

    private Map<String, Object> flattenMap(Object source) {
        if (source == null) return null;
        Map<String, Object> resultMap = new LinkedHashMap<>();
        try {
            this.doFlatten("", ".", source, resultMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultMap;
    }

    private void doFlatten(String propertyPrefix, String parting, Object inputBean, Map<String, Object> resultMap) throws Exception {

        if (StringUtil.isNotBlank(propertyPrefix)) {
            propertyPrefix = propertyPrefix + parting;
        }

        if (putNormalBean(propertyPrefix, inputBean, resultMap)) return;
        BeanInfo beanInfo = Introspector.getBeanInfo(inputBean.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {

            String propertyName = property.getName();
            if (propertyName.equals("class")) continue;

            Method getter = property.getReadMethod();
            Object value = getter.invoke(inputBean);
            if (value == null) continue;
            this.currentSource.add(value);

            String tmpPropertyPrefix = propertyPrefix;
            int index = tmpPropertyPrefix.lastIndexOf(ARRAY_SUFFIX);
            if (index > 0 && index == tmpPropertyPrefix.length() - 1) {
                tmpPropertyPrefix = tmpPropertyPrefix + ".";
            }
            tmpPropertyPrefix = tmpPropertyPrefix + propertyName;
            if (!putNormalBean(tmpPropertyPrefix, value, resultMap)) {
                doFlatten(tmpPropertyPrefix, parting, value, resultMap);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean putNormalBean(String propertyPrefix, Object bean, Map<String, Object> resultMap) throws Exception {

        if (bean.getClass().isArray()) {
            int length = Array.getLength(bean);
            Object[] objects = new Object[length];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = Array.get(bean, i);
            }
            putArrayBean(propertyPrefix, objects, resultMap);
        } else if (CommonsUtil.isInstanceProperty(Collection.class, bean)) {
            putCollectionBean(propertyPrefix, (Collection<Object>) bean, resultMap);
        } else if (CommonsUtil.isInstanceProperty(Map.class, bean)) {
            putMapBean(propertyPrefix, (Map<String, Object>) bean, resultMap);
        } else if (CommonsUtil.isSimpleProperty(bean)) {
            resultMap.put(propertyPrefix, bean);
        } else if (bean instanceof Class) {
            resultMap.put(propertyPrefix, bean);
        } else {
            return false;
        }

        return true;
    }

    private void putArrayBean(String propertyPrefix, Object[] array, Map<String, Object> resultMap) throws Exception {
        for (int i = 0; i < array.length; i++) {
            if (this.currentSource.contains(array[i])) {
                throw new IllegalArgumentException("Direct self-reference leading to cycle");
            }
            String tmpPropertyPrefix = ARRAY_PREFIX + i + ARRAY_SUFFIX;
            doFlatten(propertyPrefix, tmpPropertyPrefix, array[i], resultMap);
        }
    }

    private void putMapBean(String propertyPrefix, Map<String, Object> map, Map<String, Object> resultMap) throws Exception {

        if (StringUtil.isNotBlank(propertyPrefix)) {
            propertyPrefix = propertyPrefix + "." + MAP_PREFIX;
        }

        Set<Map.Entry<String, Object>> entries = map.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (this.currentSource.contains(entry.getValue())) {
                throw new IllegalArgumentException("Direct self-reference leading to cycle");
            }
            doFlatten(propertyPrefix, entry.getKey(), entry.getValue(), resultMap);
        }
    }

    private void putCollectionBean(String propertyPrefix, Collection<Object> collection, Map<String, Object> resultMap) throws Exception {
        putArrayBean(propertyPrefix, collection.toArray(), resultMap);
    }
}
