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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Deprecated
public class InvocableHandlerMethod extends HandlerMethod implements ProcessInvokeService {

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

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
        ReflectionUtils.makeAccessible(getBridgedMethod());
        processParam.set(args);
        try {
            return getBridgedMethod().invoke(getBean(), getMethodArgumentValues(args));
        } finally {
            processParam.remove();
        }
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
    protected Object[] getMethodArgumentValues(Map<String, Object> providedArgs) throws Exception {
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

    private static final Map<Class, Class> baseNumberType = toBaseNumberType();

    private static Map<Class, Class> toBaseNumberType() {
        Map<Class, Class> result = new HashMap<>(8);
        result.put(long.class, Long.class);
        result.put(int.class, Integer.class);
        result.put(double.class, Double.class);
        result.put(byte.class, Byte.class);
        result.put(short.class, Short.class);
        result.put(float.class, Float.class);
        result.put(boolean.class, Boolean.class);
        result.put(char.class, Character.class);
        return result;
    }

    private static Class<?> getParameterType(Class<?> clazz) {
        Class result = baseNumberType.get(clazz);
        return result == null ? clazz : result;
    }

    protected Object resolveProvidedArgument(MethodParameter parameter, Map<String, Object> providedArgs) {
        if (providedArgs == null) return null;
        Class<?> parameterType = getParameterType(parameter.getParameterType());
        if (parameterType.isInstance(providedArgs)) return providedArgs;
        Set<Map.Entry<String, Object>> entries = providedArgs.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (parameterType.isInstance(entry.getValue())) {
                //TODO:NullPointerException
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

    private Object createAttributeFromRequestValue(Object sourceValue, String attributeName, MethodParameter parameter) {
        DataBinder binder = new DataBinder(sourceValue, attributeName);
        ConversionService conversionService = binder.getConversionService();
        TypeDescriptor source = TypeDescriptor.valueOf(sourceValue.getClass());
        TypeDescriptor target = new TypeDescriptor(parameter);
        if (conversionService.canConvert(source, target)) {
            return binder.convertIfNecessary(sourceValue, parameter.getParameterType(), parameter);
        }
        return null;
    }

    protected boolean supportsParameter(MethodParameter parameter) {
        return !BeanUtils.isSimpleProperty(parameter.getParameterType());
    }

    protected String getArgumentResolutionErrorMessage(MethodParameter parameter, String text) {
        Class<?> paramType = parameter.getParameterType();
        return String.format("%s argument %d of type '%s'", text, parameter.getParameterIndex(), paramType.getName());
    }
}
