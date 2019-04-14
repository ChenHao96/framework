package org.steven.chen.component.process.handler;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.DataBinder;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.ProcessInvokeService;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class InvocableHandlerMethod extends HandlerMethod implements ProcessInvokeService {

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public InvocableHandlerMethod(ProcessHandlerService bean, Method method) {
        super(bean, method);
    }

    @Override
    public boolean isReturnVoid() {
        return isVoid();
    }

    @Override
    public Object invokeProcess(Map<String, Object> args) throws Exception {
        ReflectionUtils.makeAccessible(getBridgedMethod());
        return getBridgedMethod().invoke(getBean(), getMethodArgumentValues(args));
    }

    /**
     * 将map集合中的参数匹配到Method的参数
     * </p>
     * warn:该参数解析不支持map集合(Method的参数不支持map,其自定义参数的字段也不支持map集合)
     *
     * @param providedArgs 提供的参数
     * @return 返回对应的参数列表
     * @throws Exception
     */
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
            if (args[i] == null) {
                throw new IllegalStateException(String.format("Could not resolve method parameter at index %d in %s: %s",
                        parameter.getParameterIndex(), parameter.getMethod().toGenericString(),
                        getArgumentResolutionErrorMessage(parameter, "No suitable resolver for")));
            }
        }
        return args;
    }

    private Object resolveProvidedArgument(MethodParameter parameter, Map<String, Object> providedArgs) {
        if (providedArgs == null) return null;
        if (parameter.getParameterType().isInstance(providedArgs)) return providedArgs;
        Set<Map.Entry<String, Object>> entries = providedArgs.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (parameter.getParameterType().isInstance(entry.getValue())) {
                if (parameter.getParameterName().equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private Object resolveArgument(MethodParameter parameter, Map<String, Object> providedArgs) {
        if (providedArgs == null) return null;
        String name = parameter.getParameterName();
        Object attribute = (providedArgs.containsKey(name) ? providedArgs.get(name) : createAttribute(name, parameter, providedArgs));
        DataBinder binder = new DataBinder(attribute, name);
        binder.bind(new MutablePropertyValues(providedArgs));
        return binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
    }

    private Object createAttribute(String attributeName, MethodParameter parameter, Map<String, Object> providedArgs) {
        Object value = providedArgs.get(attributeName);
        if (value != null) {
            Object attribute = createAttributeFromRequestValue(value, attributeName, parameter);
            if (attribute != null) {
                return attribute;
            }
        }
        return BeanUtils.instantiateClass(parameter.getParameterType());
    }

    protected Object createAttributeFromRequestValue(Object sourceValue, String attributeName, MethodParameter parameter) {
        DataBinder binder = new DataBinder(sourceValue, attributeName);
        ConversionService conversionService = binder.getConversionService();
        TypeDescriptor source = TypeDescriptor.valueOf(sourceValue.getClass());
        TypeDescriptor target = new TypeDescriptor(parameter);
        if (conversionService.canConvert(source, target)) {
            return binder.convertIfNecessary(sourceValue, parameter.getParameterType(), parameter);
        }
        return null;
    }

    private boolean supportsParameter(MethodParameter parameter) {
        return !BeanUtils.isSimpleProperty(parameter.getParameterType());
    }

    private String getArgumentResolutionErrorMessage(MethodParameter parameter, String text) {
        Class<?> paramType = parameter.getParameterType();
        return String.format("%s argument %d of type '%s'", text, parameter.getParameterIndex(), paramType.getName());
    }
}
