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

import org.springframework.util.Assert;
import org.steven.chen.connect.CommonsMessage;
import org.steven.chen.connect.DefaultConnectionContext;
import org.steven.chen.connect.MessageConvertToHandlerArgs;
import org.steven.chen.utils.CommonsUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketFrameHandler extends DefaultConnectionContext implements SocketConnectionContext {

    private Socket client;
    private int connectionPort;
    private String connectionIp;
    private long lastLogTime = 0L;
    private InputStream clientInputStream;
    private OutputStream clientOutputStream;
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;

    private long noDataWaitTime;

    public SocketFrameHandler(Socket client, long noDataWaitTime) {
        Assert.notNull(client, "SocketFrameHandler Socket client is required!");

        this.client = client;
        this.noDataWaitTime = noDataWaitTime;
        this.connectionPort = client.getPort();
        this.connectionIp = CommonsUtil.socketRemoteIP(client);
        try {
            this.clientInputStream = client.getInputStream();
            this.clientOutputStream = client.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMessageConvertToHandlerArgs(MessageConvertToHandlerArgs messageConvertToHandlerArgs) {
        this.messageConvertToHandlerArgs = messageConvertToHandlerArgs;
    }

    @Override
    public String getConnectionIp() {
        return connectionIp;
    }

    @Override
    public int getConnectionPort() {
        return connectionPort;
    }

    @Override
    public CommonsMessage receiveMessage() throws IOException {
        DataInputStream dataInputStream = new DataInputStream(clientInputStream);
        if (dataInputStream.available() >= CommonsMessage.MIN_DATA_LENGTH) {
            lastLogTime = System.currentTimeMillis();
            return CommonsMessage.createMessageByData(dataInputStream);
        } else {
            long currentLogTime = System.currentTimeMillis();
            if (currentLogTime - lastLogTime > noDataWaitTime) {
                sendMessage(new CommonsMessage((byte) 0, (byte) 0));
                lastLogTime = currentLogTime;
            }
        }
        return null;
    }

    @Override
    public boolean isClose() {
        return client.isClosed();
    }

    @Override
    public void close() throws IOException {
        CommonsUtil.safeClose(clientInputStream, clientOutputStream, client);
    }

    @Override
    public void sendMessage(CommonsMessage message) throws IOException {

        if (message == null) return;
        if (clientOutputStream == null) return;

        byte[] buf = CommonsMessage.createByteByMessage(message);

        clientOutputStream.write(buf);
        clientOutputStream.flush();
    }

    @Override
    public void sendMessage(Object message) throws IOException {
        Assert.notNull(messageConvertToHandlerArgs, "MessageConvertToHandlerArgs is required!");
        sendMessage(messageConvertToHandlerArgs.convertMessageReturn(message));
    }
}
