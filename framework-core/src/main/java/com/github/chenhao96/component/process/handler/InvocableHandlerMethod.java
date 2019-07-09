package com.github.chenhao96.component.process.handler;

import com.github.chenhao96.component.process.ProcessHandlerService;
import com.github.chenhao96.component.process.ProcessInvokeService;
import org.steven.chen.utils.mapper.Jackson2FlatMapperK;

import java.lang.reflect.Method;
import java.util.Map;

public class InvocableHandlerMethod implements ProcessInvokeService {

    private ProcessHandlerService bean;
    private InvocableHandlerMethodK invocableHandlerMethodK;
    private static final Jackson2FlatMapperK jackson2FlatMapper = new Jackson2FlatMapperK();

    public InvocableHandlerMethod(ProcessHandlerService bean, Method method) {
        this.bean = bean;
        this.invocableHandlerMethodK = new InvocableHandlerMethodK(bean, method);
    }

    public ProcessHandlerService getBean() {
        return bean;
    }

    @Override
    public boolean isReturnVoid() {
        return invocableHandlerMethodK.isVoid();
    }

    @Override
    public Object invokeProcess(Map<String, Object> providedArgs) throws Exception {
        providedArgs = jackson2FlatMapper.fromFlatMapper(providedArgs);
        Object[] args = invocableHandlerMethodK.getMethodArgumentValues(providedArgs);
        return invocableHandlerMethodK.getBridgedMethod().invoke(bean, args);
    }
}
