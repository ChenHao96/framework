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

package com.github.chenhao96.component.socket;

import com.github.chenhao96.component.ComponentService;
import com.github.chenhao96.component.executor.TaskExecutorService;
import com.github.chenhao96.component.net.CommonsMessage;
import com.github.chenhao96.component.net.DefaultMessageConvertToHandlerArgs;
import com.github.chenhao96.component.net.MessageConvertToHandlerArgs;
import com.github.chenhao96.component.net.SocketHandlerTask;
import com.github.chenhao96.component.process.ProcessInvokeService;
import com.github.chenhao96.component.process.handler.HandlerFactory;
import com.github.chenhao96.model.ConfigProperty;
import com.github.chenhao96.utils.CommonsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerSocketComponent implements ComponentService {

    private static final String COMPONENT_NAME = "ServerSocketComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocketComponent.class);

    private int processors;
    private int socketPort;
    private Thread[] slackList;
    private long noDataWaitTime;
    private boolean initialize;
    private volatile boolean started;
    private ServerSocket serverSocket;
    private LinkedBlockingQueue<SocketConnectionContext> handlerQueue;

    @Resource
    private HandlerFactory handlerFactory;

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

        this.socketPort = ConfigProperty.getSocketPort();
        this.noDataWaitTime = ConfigProperty.getNoDataWaitTime();

        if (this.initialize) return;
        this.initialize = false;
        this.handlerQueue = new LinkedBlockingQueue<>();
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

    private class SocketAcceptListener implements Runnable {

        @Override
        public void run() {
            do {
                try {
                    Socket client = serverSocket.accept();
                    if (!started) {
                        CommonsUtil.safeClose(client);
                        break;
                    }
                    SocketServerConnectionContext frameHandler = new SocketServerConnectionContext(client, noDataWaitTime);
                    frameHandler.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
                    handlerQueue.add(frameHandler);
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
                    continue;
                }

                if (started) {
                    if (!handler.isClose()) {
                        handlerQueue.add(handler);
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

            SocketConnectionContext handler = null;
            try {
                handler = handlerQueue.poll(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.warn("pollHandler.", e);
            }
            if (handler == null) return null;

            CommonsMessage message = null;
            try {
                message = handler.receiveMessage();
            } catch (SocketException close) {
                CommonsUtil.safeClose(handler);
            } catch (IOException e) {
                LOGGER.warn("receiveMessage.", e);
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
