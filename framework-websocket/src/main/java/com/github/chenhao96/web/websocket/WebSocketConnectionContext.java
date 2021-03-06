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

package com.github.chenhao96.web.websocket;

import com.github.chenhao96.component.net.CommonsMessage;
import com.github.chenhao96.component.net.ConnectionCloseProcess;
import com.github.chenhao96.component.net.ConnectionContext;
import com.github.chenhao96.component.net.MessageConvertToHandlerArgs;
import com.github.chenhao96.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WebSocketConnectionContext implements ConnectionContext {

    private int connectionPort;
    private String connectionIp;
    private WebSocketSession session;
    private List<ConnectionCloseProcess> closeProcesses;
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConnectionContext.class);

    public WebSocketConnectionContext(WebSocketSession session) {
        Assert.notNull(session, "WebSocketSession session client is required!");
        this.session = session;
        this.connectionPort = session.getRemoteAddress().getPort();
        this.connectionIp = session.getRemoteAddress().getHostName();
    }

    @Override
    public String getConnectionIp() {
        return connectionIp;
    }

    @Override
    public int getConnectionPort() {
        return connectionPort;
    }

    public void setMessageConvertToHandlerArgs(MessageConvertToHandlerArgs messageConvertToHandlerArgs) {
        this.messageConvertToHandlerArgs = messageConvertToHandlerArgs;
    }

    @Override
    public boolean isClose() {
        return !session.isOpen();
    }

    @Override
    public void close() throws IOException {
        if (isClose()) return;
        if (closeProcesses != null) {
            for (ConnectionCloseProcess closeProcess : closeProcesses) {
                if (closeProcess == null) continue;
                closeProcess.process(this);
            }
        }
        session.close();
    }

    @Override
    public void sendMessage(CommonsMessage message) {
        if (isClose()) return;
        try {
            Map<String, Object> content = new HashMap<>(3);
            content.put("slaveCode", message.getSlaveCode());
            content.put("masterCode", message.getMasterCode());
            String response = new String(message.getData(), StandardCharsets.UTF_8);
            content.put("data", JsonUtils.jsonStr2JsonNodeStatic(response));
            response = JsonUtils.object2JsonStatic(content);
            session.sendMessage(new TextMessage(response));
        } catch (IOException e) {
            LOGGER.warn("sendMessage", e);
        }
    }

    @Override
    public void sendMessage(Object message) {
        if (isClose()) return;
        Assert.notNull(messageConvertToHandlerArgs, "MessageConvertToHandlerArgs is required!");
        sendMessage(messageConvertToHandlerArgs.convertMessageReturn(message));
    }

    @Override
    public void setAttribute(String name, Object obj) {
        session.getAttributes().put(name, obj);
    }

    @Override
    public Object getAttribute(String name) {
        return session.getAttributes().get(name);
    }

    @Override
    public Set<String> getAttributeNames() {
        return session.getAttributes().keySet();
    }

    @Override
    public Object removeAttribute(String name) {
        return session.getAttributes().remove(name);
    }

    @Override
    public void clearAttribute() {
        session.getAttributes().clear();
    }

    @Override
    public void addCloseProcess(ConnectionCloseProcess process) {
        if (process == null) return;
        if (closeProcesses == null) {
            closeProcesses = new LinkedList<>();
        }
        closeProcesses.add(process);
    }
}
