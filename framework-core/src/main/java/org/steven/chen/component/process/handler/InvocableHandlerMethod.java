package org.steven.chen.component.process.handler;

import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.ProcessInvokeService;
import org.steven.chen.utils.mapper.HashMapper;
import org.steven.chen.utils.mapper.Jackson2FlatMapper;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class InvocableHandlerMethod extends HandlerMethod implements ProcessInvokeService {

    private HashMapper hashMapper = new Jackson2FlatMapper();

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

    private Object[] getMethodArgumentValues(Map<String, Object> providedArgs) throws Exception {
        providedArgs = hashMapper.fromFlatMapper(providedArgs);
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            args[i] = resolveProvidedArgument(parameter, providedArgs);
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

    private String getArgumentResolutionErrorMessage(MethodParameter parameter, String text) {
        Class<?> paramType = parameter.getParameterType();
        return String.format("%s argument %d of type '%s'", text, parameter.getParameterIndex(), paramType.getName());
    }
}
