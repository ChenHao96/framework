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

package org.steven.chen.component.process.handler;

import org.springframework.core.MethodParameter;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.utils.JsonUtils;
import org.steven.chen.utils.StringUtil;
import org.steven.chen.utils.mapper.Jackson2FlatMapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NewHandlerMethod extends InvocableHandlerMethod {

    private Jackson2FlatMapper jackson2FlatMapper = new Jackson2FlatMapper();

    public NewHandlerMethod(ProcessHandlerService bean, Method method) {
        super(bean, method);
    }

    @Override
    protected Object[] getMethodArgumentValues(Map<String, Object> providedArgs) throws Exception {
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            args[i] = resolveProvidedArgument(parameter, providedArgs);
            if (args[i] != null) continue;
            if (supportsParameter(parameter)) {
                try {
                    args[i] = resolveArgument(parameter, providedArgs);
                } catch (Exception ex) {
                    throw new Exception(getArgumentResolutionErrorMessage(parameter, "Failed to resolve"), ex);
                }
            }
        }
        return args;
    }

    protected Object resolveArgument(MethodParameter parameter, Map<String, Object> providedArgs) {
        if (providedArgs == null) return null;
        String name = parameter.getParameterName();
        Map<String, Object> bean = new LinkedHashMap<>();
        Set<Map.Entry<String, Object>> entries = providedArgs.entrySet();
        resolveArgument(parameter, providedArgs, bean);
        boolean noData = bean.size() < 1;
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            if (key != null && key.startsWith(name)) {
                bean.put(key, entry.getValue());
            }
        }
        return getObject(parameter.getParameterType(), noData ? "" : name, bean);
    }

    private void resolveArgument(MethodParameter parameter, Map<String, Object> providedArgs, Map<String, Object> bean) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(parameter.getParameterType());
        } catch (IntrospectionException e) {
            return;
        }
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String propertyName = property.getName();
            if (propertyName.equals("class")) continue;
            bean.put(propertyName, providedArgs.get(propertyName));
        }
    }

    private Object getObject(Class<?> parameterType, String name, Map<String, Object> bean) {
        bean = jackson2FlatMapper.fromFlatMapper(bean);
        if (bean == null) return null;
        Object argument = StringUtil.isEmpty(name) ? bean : bean.get(name);
        if (argument == null) return null;

        try {
            return JsonUtils.jsonStr2Object(JsonUtils.object2Json(argument), parameterType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
