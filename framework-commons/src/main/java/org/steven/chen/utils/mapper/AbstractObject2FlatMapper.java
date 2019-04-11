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

import org.springframework.beans.BeanUtils;
import org.steven.chen.utils.CommonsUtil;
import org.steven.chen.utils.StringUtil;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractObject2FlatMapper implements Object2FlatMapper {

    private Object currentSource;

    @Override
    public Map<String, Object> toFlatMapper(Object object) {
        this.currentSource = object;
        try {
            return flattenMap(object);
        } finally {
            this.currentSource = null;
        }
    }

    @Override
    public <T> T fromFlatMapper(Map<String, Object> target, Class<T> clazz) {
        try {
            T result = BeanUtils.instantiateClass(clazz);
            mapFlatten(target, result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mapFlatten(Map<String, Object> target, Object resultBean) throws Exception {
        if (target == null) return;
        Set<Map.Entry<String, Object>> entrySet = target.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            doMap("", resultBean, entry);
        }
    }

    private void doMap(String propertyPrefix, Object resultBean, Map.Entry<String, Object> entry) throws Exception {

        if (StringUtil.isNotBlank(propertyPrefix)) {
            propertyPrefix = propertyPrefix + ".";
        }

        String key = entry.getKey();
        if (StringUtil.isNotBlank(key)) {
            BeanInfo beanInfo = Introspector.getBeanInfo(resultBean.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            String tmpKey = key.substring(propertyPrefix == null ? 0 : propertyPrefix.length());

            if (tmpKey.contains(".")) {
                String mapName = tmpKey.substring(0, tmpKey.indexOf("."));
                for (PropertyDescriptor property : propertyDescriptors) {
                    String propertyName = property.getName();
                    if (propertyName.equals(mapName)) {
                        Method getter = property.getReadMethod();
                        Object childBean = getter.invoke(resultBean);
                        childBean = childBean == null ? BeanUtils.instantiateClass(property.getPropertyType()) : childBean;
                        Method setter = property.getWriteMethod();
                        setter.invoke(resultBean, childBean);

                        doMap(propertyPrefix + mapName, childBean, entry);
                        break;
                    }
                }
            } else if (key.startsWith(propertyPrefix)) {
                for (PropertyDescriptor property : propertyDescriptors) {
                    String propertyName = property.getName();
                    if (propertyName.equals(tmpKey)) {
                        Method setter = property.getWriteMethod();
                        setter.invoke(resultBean, entry.getValue());
                    }
                }
            }
        }
    }

    private Map<String, Object> flattenMap(Object source) {
        if (source == null) return null;
        Map<String, Object> resultMap = new HashMap<>();
        try {
            this.doFlatten("", source, resultMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultMap;
    }

    private void doFlatten(String propertyPrefix, Object inputBean, Map<String, Object> resultMap) throws Exception {

        if (StringUtil.isNotBlank(propertyPrefix)) {
            propertyPrefix = propertyPrefix + ".";
        }

        if (puNormalBean(propertyPrefix, inputBean, resultMap)) return;
        BeanInfo beanInfo = Introspector.getBeanInfo(inputBean.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {

            String propertyName = property.getName();
            if (propertyName.equals("class")) continue;

            Method getter = property.getReadMethod();
            Object value = getter.invoke(inputBean);
            if (value == null) continue;
            if (value == this.currentSource) {
                throw new IllegalArgumentException("Direct self-reference leading to cycle.");
            }

            String tmpPropertyPrefix = propertyPrefix + propertyName;
            if (!puNormalBean(tmpPropertyPrefix, value, resultMap)) {
                doFlatten(tmpPropertyPrefix, value, resultMap);
            }
        }
    }

    private boolean puNormalBean(String propertyPrefix, Object bean, Map<String, Object> resultMap) {
        if (CommonsUtil.isInstanceProperty(Collection.class, bean)) {
            putCollectionBean(propertyPrefix, bean, resultMap);
        } else if (CommonsUtil.isInstanceProperty(Map.class, bean)) {
            putMapBean(propertyPrefix, bean, resultMap);
        } else if (CommonsUtil.isSimpleProperty(bean)) {
            resultMap.put(propertyPrefix, bean);
        } else {
            return false;
        }
        return true;
    }

    protected abstract void putMapBean(String propertyPrefix, Object bean, Map<String, Object> resultMap);

    protected abstract void putCollectionBean(String propertyPrefix, Object bean, Map<String, Object> resultMap);
}
