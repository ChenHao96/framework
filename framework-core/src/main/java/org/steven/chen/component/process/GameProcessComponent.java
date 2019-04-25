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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.steven.chen.component.ComponentService;
import org.steven.chen.component.process.handler.HandlerFactoryImpl;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

@Component
public class GameProcessComponent implements ComponentService {

    private static final String COMPONENT_NAME = "GameProcessComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(GameProcessComponent.class);

    private boolean initialized;

    @Resource
    private HandlerFactoryImpl handlerFactory;

    @Autowired(required = false)
    private List<ProcessHandlerService> processHandlerServices;

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

        if (this.initialized) return;

        initialized = false;
        if (!CollectionUtils.isEmpty(processHandlerServices)) {
            for (ProcessHandlerService service : processHandlerServices) {
                if (service == null) continue;

                boolean threadSafety = false;
                HandlerMapping classAnnotation = service.getClass().getAnnotation(HandlerMapping.class);
                if (service.getClass().getAnnotation(AsyncHandler.class) != null) threadSafety = true;
                byte masterCode = classAnnotation == null ? 0 : classAnnotation.value();

                Method[] methods = service.getClass().getDeclaredMethods();
                if (methods == null || methods.length == 0) continue;

                for (Method method : methods) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        LOGGER.warn("{} Method is not public!", method.getName());
                        continue;
                    }

                    HandlerMapping methodAnnotation = method.getAnnotation(HandlerMapping.class);
                    byte slaveCode = methodAnnotation == null ? 0 : methodAnnotation.value();
                    if (!threadSafety && method.getAnnotation(AsyncHandler.class) != null) threadSafety = true;

                    handlerFactory.addHandler(masterCode, slaveCode, service, method, threadSafety);
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
