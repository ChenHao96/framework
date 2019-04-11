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

package org.steven.chen.web.websocket;

import org.springframework.util.Assert;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.steven.chen.connect.CommonsMessage;
import org.steven.chen.connect.ConnectionContext;
import org.steven.chen.connect.MessageConvertToHandlerArgs;
import org.steven.chen.utils.JsonUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class WebSocketFrameHandler implements ConnectionContext {

    private int connectionPort;
    private String connectionIp;
    private WebSocketSession session;
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;

    public WebSocketFrameHandler(WebSocketSession session) {
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
        return session.isOpen();
    }

    @Override
    public void close() throws IOException {
        session.close();
    }

    @Override
    public void sendMessage(CommonsMessage message) throws IOException {
        session.sendMessage(new TextMessage(JsonUtils.object2Json(message)));
    }

    @Override
    public void sendMessage(Object message) throws IOException {
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
        return Collections.unmodifiableSet(session.getAttributes().keySet());
    }

    @Override
    public Object removeAttribute(String name) {
        return session.getAttributes().remove(name);
    }

    @Override
    public void clearAttribute() {
        session.getAttributes().clear();
    }
}
