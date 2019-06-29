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

package org.steven.chen.component.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.steven.chen.component.ComponentService;
import org.steven.chen.model.ConfigProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TaskExecutorComponent implements ComponentService, TaskExecutorService {

    private static final String COMPONENT_NAME = "TaskExecutorComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorComponent.class);

    private int poolSize;
    private boolean runnable;
    private boolean initialized = false;
    private boolean rebuildExecutor = false;
    private ExecutorService handlerExecutor;
    private ScheduledExecutorService scheduler;

    @Autowired(required = false)
    private ConfigProperty configProperty;

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
        return runnable;
    }

    @Override
    public void initialize() throws Exception {

        int cachePoolSize = ConfigProperty.getThreadPoolSize();
        if (cachePoolSize > poolSize) {
            rebuildExecutor = true;
            poolSize = cachePoolSize;
        }

        if (this.initialized) {
            LOGGER.warn("{} initialize,do not repeat initialize,please!", COMPONENT_NAME);
            return;
        }

        initialized = true;
    }

    @Override
    public void start() throws Exception {

        if (!this.initialized) {
            LOGGER.warn("initialize is not success.");
            return;
        } else if (runnable) return;

        runnable = false;
        checkExecutor();
        if (rebuildExecutor) {
            scheduler = Executors.newScheduledThreadPool(poolSize);
            handlerExecutor = Executors.newFixedThreadPool(poolSize);
            rebuildExecutor = false;
        }

        runnable = true;
    }

    private void checkExecutor() throws InterruptedException {
        checkExecutorShutdown(handlerExecutor);
        checkExecutorShutdown(scheduler);
    }

    private void checkExecutorShutdown(ExecutorService executor) throws InterruptedException {
        if (executor != null) {
            if (!executor.isShutdown()) {
                executor.shutdown();
                while (!executor.isTerminated()) {
                    Thread.sleep(1000);
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (!runnable) return;
        checkExecutor();
        runnable = false;
    }

    @Override
    public void addHandler(Runnable task) {
        if (!runnable) return;
        handlerExecutor.submit(task);
    }

    @Override
    public void addHandlerDelay(Runnable task, long delay, TimeUnit unit) {
        addHandlerDelay(task, 0, delay, unit);
    }

    @Override
    public void addHandlerDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        if (!runnable) return;
        scheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    @Override
    public void addHandlerRate(Runnable task, long period, TimeUnit unit) {
        addHandlerRate(task, 0, period, unit);
    }

    @Override
    public void addHandlerRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        if (!runnable) return;
        scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
}
