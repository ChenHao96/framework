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
import org.springframework.beans.factory.annotation.Autowired;
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

public class ServerSocketComponent implements ComponentService {

    private static final String COMPONENT_NAME = "ServerSocketComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocketComponent.class);

    private int processors;
    private int socketPort;
    private boolean started;
    private long noDataWaitTime;
    protected boolean initialize;
    private ServerSocket serverSocket;
    private Thread[] slackList;
    private final Object wait = new Object();
    private Queue<SocketConnectionContext> handlerQueue;

    @Resource
    private HandlerFactory handlerFactory;

    @Autowired(required = false)
    private ConfigProperty configProperty;

    @Resource
    private TaskExecutorService executorService;

    @Autowired(required = false)
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;

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

        this.socketPort = configProperty == null ? ConfigProperty.DEFAULT_SOCKET_PORT : configProperty.getSocketPort();
        this.noDataWaitTime = configProperty == null ? ConfigProperty.DEFAULT_NO_DATA_WAIT_TIME : configProperty.getNoDataWaitTime();
        if (this.initialize) return;

        this.initialize = false;
        this.handlerQueue = new ConcurrentLinkedQueue<>();
        this.processors = (Runtime.getRuntime().availableProcessors() / 2) + 1;
        this.slackList = new Thread[this.processors];
        if (this.messageConvertToHandlerArgs == null) {
            this.messageConvertToHandlerArgs = new DefaultMessageConvertToHandlerArgs();
        }
        this.initialize = true;
    }

    @Override
    public void start() throws Exception {
        if (this.started) return;
        if (!this.initialize) {
            LOGGER.warn("initialize is not success.");
            return;
        }

        this.serverSocket = new ServerSocket(this.socketPort, 128);
        new Thread(new SocketAcceptListener(), COMPONENT_NAME).start();
        ThreadGroup group = new ThreadGroup(COMPONENT_NAME + "-shl");
        for (int i = 0; i < this.processors; i++) {
            this.slackList[i] = new Thread(group, new SocketHandlerListener());
            this.slackList[i].start();
        }
        this.started = true;
    }

    @Override
    public void stop() throws Exception {

        if (!this.started) return;
        this.started = false;
        synchronized (wait) {
            wait.notifyAll();
        }

        boolean listenerRunning = false;
        do {
            for (Thread thread : slackList) {
                if (thread != null) {
                    Thread.State state = thread.getState();
                    if (!Thread.State.TERMINATED.equals(state)) {
                        listenerRunning = true;
                    }
                }
            }
        } while (listenerRunning);

        try {
            this.serverSocket.close();
        } catch (IOException e) {
            LOGGER.warn("ServerSocketComponent shutdown.", e);
        }
    }

    private void incrementHandler(SocketConnectionContext handler) {
        this.handlerQueue.add(handler);
        synchronized (wait) {
            wait.notify();
        }
    }

    private class SocketAcceptListener implements Runnable {

        @Override
        public void run() {
            do {
                try {
                    Socket client = serverSocket.accept();
                    if (!started) break;
                    SocketServerConnectionContext frameHandler = new SocketServerConnectionContext(client, noDataWaitTime);
                    frameHandler.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
                    incrementHandler(frameHandler);
                } catch (IOException e) {
                    LOGGER.warn("serverSocket accept exception.", e);
                }
            } while (started);
        }
    }

    private class SocketHandlerListener implements Runnable {

        @Override
        public void run() {
            while (true) {
                SocketConnectionContext handler = pollHandler();
                if (handler == null) {
                    if (!started) break;
                    synchronized (wait) {
                        try {
                            wait.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            LOGGER.warn("pollHandler wait.", e);
                        }
                    }
                    continue;
                }

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
            }
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
