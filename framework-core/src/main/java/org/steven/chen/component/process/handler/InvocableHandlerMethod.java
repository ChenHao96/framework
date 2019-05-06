package org.steven.chen.component.process.handler;

import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.ProcessInvokeService;

import java.lang.reflect.Method;
import java.util.Map;

public class InvocableHandlerMethod implements ProcessInvokeService {

    private ProcessHandlerService bean;
    private InvocableHandlerMethodK invocableHandlerMethodK;

    private static final ThreadLocal<Map<String, Object>> processParam = new ThreadLocal<>();

    public InvocableHandlerMethod(ProcessHandlerService bean, Method method) {
        this.bean = bean;
        this.invocableHandlerMethodK = new InvocableHandlerMethodK(bean, method);
    }

    public static Map<String, Object> getProcessParam() {
        return processParam.get();
    }

    public ProcessHandlerService getBean() {
        return bean;
    }

    @Override
    public boolean isReturnVoid() {
        return invocableHandlerMethodK.isVoid();
    }

    @Override
    public Object invokeProcess(Map<String, Object> args) throws Exception {
        processParam.set(args);
        try {
            return invocableHandlerMethodK.invokeProcess(args);
        } finally {
            processParam.remove();
        }
    }
}
