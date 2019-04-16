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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.steven.chen.component.ComponentService;
import org.steven.chen.model.ConfigProperty;

import javax.annotation.Resource;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TaskExecutorComponent implements ComponentService, TaskExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorComponent.class);

    private static final String COMPONENT_NAME = "TaskExecutorComponent";

    private boolean empty;
    private boolean runnable;
    private boolean initialized;
    private Queue<Runnable> taskQueue;
    private ExecutorService handlerExecutor;
    private final AtomicInteger wait = new AtomicInteger();

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
        return runnable;
    }

    @Override
    public void initialize() throws Exception {
        empty = true;
        runnable = initialized = false;
        taskQueue = new ConcurrentLinkedQueue<>();
        int threadPoolSize = getConfigThreadPoolSize();
        if (threadPoolSize < 1) {
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            threadPoolSize = (int) (availableProcessors * 2.5);
        }
        handlerExecutor = Executors.newFixedThreadPool(threadPoolSize);
        initialized = true;
    }

    private int getConfigThreadPoolSize() {
        try {
            ConfigProperty result = applicationContext.getBean(ConfigProperty.class);
            return result.getThreadPoolSize();
        } catch (Exception e) {
            LOGGER.warn("getConfigThreadPoolSize", e);
        }
        return 0;
    }

    public void addHandler(Runnable task) {
        if (!runnable) return;
        taskQueue.add(task);
        synchronized (wait) {
            wait.incrementAndGet();
            wait.notify();
            empty = false;
        }
    }

    @Override
    public void start() throws Exception {
        if (runnable) return;
        new Thread(new TaskThreadRunnable(), COMPONENT_NAME).start();
        runnable = true;
    }

    @Override
    public void stop() throws Exception {
        if (!runnable) return;
        runnable = false;
        if (empty) {
            synchronized (wait) {
                wait.notify();
            }
        }
        handlerExecutor.shutdown();
    }

    private class TaskThreadRunnable implements Runnable {
        @Override
        public void run() {
            do {
                synchronized (wait) {
                    try {
                        if (wait.decrementAndGet() < 0) {
                            wait.wait();
                            if (!runnable) break;
                        }
                    } catch (InterruptedException e) {
                        LOGGER.warn("TaskThreadRunnable run.", e);
                    }
                }

                Runnable task = taskQueue.poll();
                if (task != null) {
                    handlerExecutor.submit(task);
                }
            } while (runnable);
        }
    }
}
