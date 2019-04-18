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

package org.steven.chen.component.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.steven.chen.component.ComponentService;
import org.steven.chen.component.executor.TaskExecutorService;
import org.steven.chen.component.process.ProcessInvokeService;
import org.steven.chen.component.process.handler.HandlerFactory;
import org.steven.chen.component.socket.connect.CommonsMessage;
import org.steven.chen.component.socket.connect.DefaultMessageConvertToHandlerArgs;
import org.steven.chen.component.socket.connect.MessageConvertToHandlerArgs;
import org.steven.chen.component.socket.connect.SocketHandlerTask;
import org.steven.chen.model.ConfigProperty;
import org.steven.chen.utils.CommonsUtil;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerSocketComponent implements ComponentService {

    private static final String COMPONENT_NAME = "ServerSocketComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocketComponent.class);

    private boolean empty;
    private int processors;
    private int socketPort;
    private boolean started;
    private boolean runnable;
    private boolean shutdown;
    private long noDataWaitTime;
    protected boolean initialize;
    private ExecutorService executor;
    private ServerSocket serverSocket;
    private Queue<SocketConnectionContext> handlerQueue;
    private final AtomicInteger wait = new AtomicInteger();
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;

    @Resource
    private HandlerFactory handlerFactory;

    @Resource
    private TaskExecutorService executorService;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public boolean initialized() {
        return initialize;
    }

    @Override
    public boolean started() {
        return started;
    }

    @Override
    public void initialize() throws Exception {
        if (this.initialize) {
            LOGGER.warn("{} initialize,do not repeat initialize,please!",COMPONENT_NAME);
            return;
        }

        this.initialize = false;
        loadMessageConvert();
        socketPort = ConfigProperty.getSocketPort();
        handlerQueue = new ConcurrentLinkedQueue<>();
        executor = Executors.newFixedThreadPool(processors);
        noDataWaitTime = ConfigProperty.getNoDataWaitTime();
        processors = (Runtime.getRuntime().availableProcessors() / 2) + 1;
        empty = initialize = true;
    }

    private void loadMessageConvert() {
        try {
            messageConvertToHandlerArgs = applicationContext.getBean(MessageConvertToHandlerArgs.class);
        } catch (NoSuchBeanDefinitionException e) {
            messageConvertToHandlerArgs = new DefaultMessageConvertToHandlerArgs();
        }
    }

    @Override
    public void start() throws Exception {
        serverSocket = new ServerSocket(socketPort, 128);
        new Thread(new SocketAcceptListener(), COMPONENT_NAME).start();
        started = runnable = true;
        ThreadGroup group = new ThreadGroup(COMPONENT_NAME + "-shl");
        for (int i = 0; i < processors; i++) {
            new Thread(group, new SocketHandlerListener()).start();
        }
    }

    @Override
    public void stop() throws Exception {

        shutdown = true;
        runnable = started = false;

        if (empty) {
            synchronized (wait) {
                wait.notifyAll();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.warn("ServerSocketComponent shutdown.", e);
        }

        executor.shutdown();
    }

    private void incrementHandler(SocketConnectionContext handler) {
        handlerQueue.add(handler);
        synchronized (wait) {
            wait.incrementAndGet();
            wait.notify();
            empty = false;
        }
    }

    private class SocketAcceptListener implements Runnable {

        @Override
        public void run() {

            do {
                try {
                    Socket client = serverSocket.accept();
                    if (shutdown) break;
                    SocketFrameHandler frameHandler = new SocketFrameHandler(client, noDataWaitTime);
                    frameHandler.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
                    incrementHandler(frameHandler);
                } catch (IOException e) {
                    if (shutdown) break;
                    LOGGER.warn("serverSocket accept exception.", e);
                }
            } while (true);
        }
    }

    private class SocketHandlerListener implements Runnable {

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
                        LOGGER.warn("SocketHandlerListener wait.", e);
                    }
                }

                SocketConnectionContext handler = pollHandler();
                if (handler == null) continue;

                if (started) {
                    if (!handler.isClose()) {
                        incrementHandler(handler);
                    }
                } else {
                    try {
                        handler.close();
                    } catch (Exception e) {
                        LOGGER.warn("SocketHandlerListener close.", e);
                    }
                }
            } while (true);
        }

        private SocketConnectionContext pollHandler() {

            SocketConnectionContext handler = handlerQueue.poll();
            if (handler == null) return null;

            CommonsMessage message = null;
            try {
                message = handler.receiveMessage();
            } catch (SocketException close) {
                CommonsUtil.safeClose(handler);
            } catch (IOException e) {
                LOGGER.warn("SocketHandlerListener pollHandler.", e);
            }

            return addHandler(handler, message);
        }

        private SocketConnectionContext addHandler(SocketConnectionContext handler, CommonsMessage message) {

            if (message != null) {
                ProcessInvokeService invokeService = handlerFactory.getProcessMethod(
                        message.getMasterCode(), message.getSlaveCode());
                if (invokeService != null) {
                    SocketHandlerTask task = new SocketHandlerTask(message);
                    task.setConnectionContext(handler);
                    task.setInvokeService(invokeService);
                    task.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
                    executorService.addHandler(task);
                } else {
                    LOGGER.warn("NoSuchHandlerDefinition. masterCode:{}, slaveCode:{}", message.getMasterCode(), message.getSlaveCode());
                }
            }

            return handler;
        }
    }
}
