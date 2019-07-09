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

package com.github.chenhao96.component.manager;

import com.github.chenhao96.component.ComponentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class ComponentManagerBean implements ComponentManager {

    private static final String COMPONENT_NAME = "ComponentManagerBean";
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentManagerBean.class);

    @Autowired(required = false)
    private List<ComponentService> componentServiceList;

    private boolean error = false;
    private boolean startup = false;
    private boolean initialized = false;

    public boolean isStartup() {
        return this.startup;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void initialize() {

        if (this.initialized) {
            LOGGER.warn("{} initialize,do not repeat initialize,please!", COMPONENT_NAME);
            return;
        }

        this.error = false;
        this.initialized = false;
        LOGGER.info("initialize beginning...");

        if (!CollectionUtils.isEmpty(componentServiceList)) {
            componentServiceList.stream().filter(service -> service != null).forEach(service -> {
                try {
                    service.initialize();
                } catch (Exception e) {
                    this.error = true;
                    LOGGER.warn("{} initialize exception!", service.getComponentName(), e);
                }
            });
        }

        if (this.error) {
            LOGGER.warn("initialize have error!");
        } else {
            this.initialized = true;
            LOGGER.info("initialize finish.");
        }
    }

    public void startup() {

        if (this.startup) return;

        if (!this.initialized) {
            LOGGER.warn("startup,initialize is not success.");
            return;
        }

        if (this.error) {
            LOGGER.warn("startup,initialize error.");
            return;
        }

        this.error = false;
        this.startup = false;
        LOGGER.info("startup beginning...");

        if (!CollectionUtils.isEmpty(componentServiceList)) {
            componentServiceList.stream().filter(service -> service != null).forEach(service -> {
                try {
                    if (service.initialized()) {
                        service.start();
                    }
                } catch (Exception e) {
                    this.error = true;
                    LOGGER.warn("{} startup exception!", service.getComponentName(), e);
                }
            });
        }

        if (this.error) {
            LOGGER.warn("startup have error!");
        } else {
            this.startup = true;
            LOGGER.info("startup finish.");
        }
    }

    public void shutdown() {

        if (!this.startup) {
            LOGGER.warn("shutdown,manager is not startup.");
            return;
        }

        this.error = false;
        LOGGER.info("shutdown beginning...");

        if (!CollectionUtils.isEmpty(componentServiceList)) {
            componentServiceList.stream().filter(service -> service != null).forEach(service -> {
                try {
                    if (service.started()) {
                        service.stop();
                    }
                } catch (Exception e) {
                    this.error = true;
                    LOGGER.warn("{} shutdown exception!", service.getComponentName(), e);
                }
            });
        }

        this.startup = false;
        if (this.error) {
            LOGGER.warn("shutdown have error!");
        } else {
            LOGGER.info("shutdown finish.");
        }
    }
}
