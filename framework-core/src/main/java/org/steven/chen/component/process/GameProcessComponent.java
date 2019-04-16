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

package org.steven.chen.component.process;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.steven.chen.component.ComponentService;
import org.steven.chen.component.process.handler.HandlerFactoryImpl;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Component
public class GameProcessComponent implements ComponentService {

    private static final String COMPONENT_NAME = "GameProcessComponent";

    private boolean initialized;

    @Resource
    private HandlerFactoryImpl handlerFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    @Override
    public boolean started() {
        return true;
    }

    @Override
    public void initialize() throws Exception {

        initialized = false;
        Map<String, ProcessHandlerService> beanMap = applicationContext.getBeansOfType(ProcessHandlerService.class);
        if (beanMap.size() > 0) {
            Set<Map.Entry<String, ProcessHandlerService>> arrays = beanMap.entrySet();
            for (Map.Entry<String, ProcessHandlerService> entry : arrays) {

                ProcessHandlerService service = entry.getValue();
                if (service == null) continue;

                GameProcessAnnotation classAnnotation = service.getClass().getAnnotation(GameProcessAnnotation.class);
                byte masterCode = 0;
                if (classAnnotation != null) {
                    masterCode = classAnnotation.code();
                }

                Method[] methods = service.getClass().getDeclaredMethods();
                if (methods == null || methods.length == 0) continue;

                for (Method method : methods) {
                    GameProcessAnnotation methodAnnotation = method.getAnnotation(GameProcessAnnotation.class);
                    if (methodAnnotation != null) {
                        byte slaveCode = methodAnnotation.code();
                        handlerFactory.addHandler(masterCode, slaveCode, service, method, methodAnnotation.threadSafety());
                    }
                }
            }
        }

        handlerFactory.addHandlerStop();
        initialized = true;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }
}
