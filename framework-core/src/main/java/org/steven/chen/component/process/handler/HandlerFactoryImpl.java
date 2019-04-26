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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.ProcessInvokeService;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Deprecated
public class HandlerFactoryImpl implements HandlerFactory, InnerHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerFactoryImpl.class);

    private boolean addFinish = false;
    private Map<Byte, Map<Byte, Method>> serviceMethod;
    private Map<Byte, Map<Byte, ProcessHandlerService>> classBeans;
    private Map<Byte, Map<Byte, ProcessInvokeService>> process;

    @Override
    public ProcessInvokeService getProcessMethod(byte slaveCode) {
        return getProcessMethod((byte) 0, slaveCode);
    }

    @Override
    public ProcessInvokeService getProcessMethod(byte masterCode, byte slaveCode) {
        if (!this.addFinish) LOGGER.warn("handlerFactory is not stop add handler.");
        if (this.process == null) return null;
        Map<Byte, ProcessInvokeService> invokeServiceMap = this.process.get(masterCode);
        if (invokeServiceMap == null) return null;
        return invokeServiceMap.get(slaveCode);
    }

    @Override
    public void addHandlerStop() {

        if (this.serviceMethod != null) {
            Set<Map.Entry<Byte, Map<Byte, Method>>> methods = this.serviceMethod.entrySet();
            for (Map.Entry<Byte, Map<Byte, Method>> entry : methods) {
                if (entry == null || entry.getValue() == null) continue;
                entry.getValue().clear();
            }
            this.serviceMethod.clear();
        }

        if (this.classBeans != null) {
            Set<Map.Entry<Byte, Map<Byte, ProcessHandlerService>>> methods = this.classBeans.entrySet();
            for (Map.Entry<Byte, Map<Byte, ProcessHandlerService>> entry : methods) {
                if (entry == null || entry.getValue() == null) continue;
                entry.getValue().clear();
            }
            this.classBeans.clear();
        }

        this.classBeans = null;
        this.serviceMethod = null;
        this.addFinish = true;
    }

    @Override
    public void addHandler(byte masterCode, byte slaveCode, ProcessHandlerService bean, Method handler, boolean threadSafety) {

        if (bean == null || handler == null) return;
        if (masterCode == 0 || slaveCode == 0) return;

        Map<Byte, ProcessHandlerService> beanMap = getProcessInterface(masterCode);
        beanMap.put(slaveCode, bean);
        this.classBeans.put(masterCode, beanMap);

        Map<Byte, Method> methodMap = getProcessMethodMap(masterCode);
        Method cacheMethod = methodMap.get(slaveCode);
        if (cacheMethod != null) {
            ProcessHandlerService cacheBean = beanMap.get(slaveCode);
            LOGGER.warn("HandlerFactory code Repeated coverage masterCode:{},slaveCode:{},cacheBean:{},cacheMethod:{},currentBean:{},currentMethod:{}",
                    masterCode, slaveCode, cacheBean.getClassName(), cacheMethod.getName(), bean.getClassName(), handler.getName());
        }
        methodMap.put(slaveCode, handler);
        this.serviceMethod.put(masterCode, methodMap);

        Map<Byte, ProcessInvokeService> invokeServiceMap = getProcessInvoke(masterCode);
        invokeServiceMap.put(slaveCode, putHandlerMethod(threadSafety, bean, handler));
        this.process.put(masterCode, invokeServiceMap);
    }

    protected ProcessInvokeService putHandlerMethod(boolean threadSafety, ProcessHandlerService bean, Method handler) {
        return threadSafety ? new InvocableHandlerMethodThreadSafety(bean, handler) : new InvocableHandlerMethod(bean, handler);
    }

    private Map<Byte, Method> getProcessMethodMap(byte masterCode) {
        this.serviceMethod = this.serviceMethod == null ? new TreeMap<>() : this.serviceMethod;
        Map<Byte, Method> methodMap = this.serviceMethod.get(masterCode);
        methodMap = methodMap == null ? new TreeMap<>() : methodMap;
        return methodMap;
    }

    private Map<Byte, ProcessHandlerService> getProcessInterface(byte masterCode) {
        this.classBeans = this.classBeans == null ? new TreeMap<>() : this.classBeans;
        Map<Byte, ProcessHandlerService> beanMap = this.classBeans.get(masterCode);
        beanMap = beanMap == null ? new TreeMap<>() : beanMap;
        return beanMap;
    }

    private Map<Byte, ProcessInvokeService> getProcessInvoke(byte masterCode) {
        this.process = this.process == null ? new TreeMap<>() : this.process;
        Map<Byte, ProcessInvokeService> invokeServiceMap = this.process.get(masterCode);
        invokeServiceMap = invokeServiceMap == null ? new TreeMap<>() : invokeServiceMap;
        return invokeServiceMap;
    }
}
