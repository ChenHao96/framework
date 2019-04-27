package org.steven.chen.component.process.handler;

import org.springframework.beans.BeanUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.ProcessInvokeService;
import org.steven.chen.utils.BaseDataTypeUtil;
import org.steven.chen.utils.JsonUtils;
import org.steven.chen.utils.StringUtil;
import org.steven.chen.utils.mapper.Jackson2FlatMapper;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class InvocableHandlerMethod extends HandlerMethod implements ProcessInvokeService {

    private static final Jackson2FlatMapper jackson2FlatMapper = new Jackson2FlatMapper();

    protected ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private static final ThreadLocal<Map<String, Object>> processParam = new ThreadLocal<>();

    public InvocableHandlerMethod(ProcessHandlerService bean, Method method) {
        super(bean, method);
    }

    public static Map<String, Object> getProcessParam() {
        return processParam.get();
    }

    @Override
    public boolean isReturnVoid() {
        return isVoid();
    }

    @Override
    public Object invokeProcess(Map<String, Object> args) throws Exception {
        processParam.set(args);
        try {
            return invoke(jackson2FlatMapper.fromFlatMapper(args));
        } finally {
            processParam.remove();
        }
    }

    private Object invoke(Map<String, Object> args) throws Exception {
        ReflectionUtils.makeAccessible(getBridgedMethod());
        return getBridgedMethod().invoke(getBean(), getMethodArgumentValues(args));
    }

    private Object[] getMethodArgumentValues(Map<String, Object> providedArgs) throws Exception {
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args[i] = resolveProvidedArgument(parameter, providedArgs);
            if (args[i] != null) continue;
            if (supportsParameter(parameter)) {
                try {
                    args[i] = resolveArgument(parameter, providedArgs);
                    continue;
                } catch (Exception ex) {
                    throw new Exception(getArgumentResolutionErrorMessage(parameter, "Failed to resolve"), ex);
                }
            }
            if (BaseDataTypeUtil.isBaseDataType(parameter.getParameterType())) {
                throw new IllegalStateException(String.format("Could not resolve method parameter at index %d in %s: %s",
                        parameter.getParameterIndex(), parameter.getMethod().toGenericString(),
                        getArgumentResolutionErrorMessage(parameter, "No suitable resolver for")));
            }
        }
        return args;
    }

    private Object resolveProvidedArgument(MethodParameter parameter, Map<String, Object> providedArgs) {
        if (providedArgs == null) return null;
        String parameterName = parameter.getParameterName();
        Class<?> parameterType = BaseDataTypeUtil.baseDataType2BoxDataType(parameter.getParameterType());
        Set<Map.Entry<String, Object>> entries = providedArgs.entrySet();
        if (parameterType.isInstance(providedArgs)) {
            MapParameter mapParameter = parameter.getParameterAnnotation(MapParameter.class);
            if (mapParameter == null || StringUtil.isEmpty(mapParameter.value())) return null;
            return processMapParameter(mapParameter.value(), entries);
        }
        for (Map.Entry<String, Object> entry : entries) {
            if (parameterName.equals(entry.getKey())) {
                if (parameterType.isInstance(entry.getValue())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> processMapParameter(String prefix, Set<Map.Entry<String, Object>> entries) {
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            if (key != null && key.equals(prefix)) {
                return (Map<String, Object>) entry.getValue();
            }
        }
        return null;
    }

    private Object resolveArgument(MethodParameter parameter, Map<String, Object> providedArgs) throws IOException {

        if (providedArgs == null) return null;
        String parameterName = parameter.getParameterName();
        Object parameterValue = providedArgs.get(parameterName);
        if (parameterValue == null) return null;

        if (parameter.getParameterType().isInterface()) {
            return JsonUtils.jsonStr2Object(JsonUtils.object2Json(parameterValue), parameter.getParameterType());
        }

        try {
            Object inputBean = BeanUtils.instantiateClass(parameter.getParameterType());
            return paramBind(inputBean, parameterValue, providedArgs);
        } catch (Exception e) {
            return null;
        }
    }

    private Object paramBind(Object inputBean, Object parameterValue, Map<String, Object> providedArgs) throws Exception {
        if (inputBean instanceof Collection) {
            inputBean = JsonUtils.jsonStr2Object(JsonUtils.object2Json(parameterValue), inputBean.getClass());
        } else {
            BeanInfo beanInfo = Introspector.getBeanInfo(inputBean.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String propertyName = property.getName();
                if (propertyName.equals("class")) continue;
                Method setter = property.getWriteMethod();
                Object value = providedArgs.get(propertyName);
                if (value == null) continue;
                Class parameterType = setter.getParameterTypes()[0];
                if (!parameterType.isInstance(value)) {
                    value = JsonUtils.jsonStr2Object(JsonUtils.object2Json(value), parameterType);
                }
                setter.invoke(inputBean, value);
            }
        }
        return inputBean;
    }

    private boolean supportsParameter(MethodParameter parameter) {
        return !BeanUtils.isSimpleProperty(parameter.getParameterType());
    }

    private String getArgumentResolutionErrorMessage(MethodParameter parameter, String text) {
        Class<?> paramType = parameter.getParameterType();
        return String.format("%s argument %d of type '%s'", text, parameter.getParameterIndex(), paramType.getName());
    }
}
