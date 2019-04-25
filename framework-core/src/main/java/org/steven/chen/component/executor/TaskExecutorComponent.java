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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TaskExecutorComponent implements ComponentService, TaskExecutorService {

    private static final String COMPONENT_NAME = "TaskExecutorComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorComponent.class);

    private int poolSize;
    private boolean runnable;
    private boolean initialized;
    private Queue<Runnable> taskQueue;
    private ExecutorService handlerExecutor;
    private final Object wait = new Object();

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

        if (this.initialized) {
            if (configProperty != null && configProperty.getThreadPoolSize() > poolSize) {
                poolSize = configProperty.getThreadPoolSize();
                if (handlerExecutor.isTerminated() || handlerExecutor.isShutdown()) {
                    handlerExecutor = Executors.newFixedThreadPool(poolSize);
                }
            }
            LOGGER.warn("{} initialize,do not repeat initialize,please!", COMPONENT_NAME);
            return;
        }

        runnable = initialized = false;
        taskQueue = new ConcurrentLinkedQueue<>();
        poolSize = configProperty == null ? ConfigProperty.DEFAULT_THREAD_POOL_SIZE : configProperty.getThreadPoolSize();
        handlerExecutor = Executors.newFixedThreadPool(poolSize);
        initialized = true;
    }

    public void addHandler(Runnable task) {
        if (!runnable) return;
        taskQueue.add(task);
        synchronized (wait) {
            wait.notify();
        }
    }

    @Override
    public void start() throws Exception {
        if (runnable) return;
        if (!this.initialized) {
            LOGGER.warn("initialize is not success.");
            return;
        }
        new Thread(new TaskThreadRunnable(), COMPONENT_NAME).start();
        runnable = true;
    }

    @Override
    public void stop() throws Exception {
        if (!runnable) return;
        runnable = false;
        synchronized (wait) {
            wait.notify();
        }
        handlerExecutor.shutdown();
        while (!handlerExecutor.isTerminated()) {
            Thread.sleep(1000);
        }
    }

    private class TaskThreadRunnable implements Runnable {
        @Override
        public void run() {
            while (runnable) {
                Runnable task = taskQueue.poll();
                if (task == null) {
                    synchronized (wait) {
                        try {
                            wait.wait();
                        } catch (InterruptedException e) {
                            LOGGER.warn("", e);
                        }
                    }
                    continue;
                }
                handlerExecutor.submit(task);
            }
        }
    }
}
