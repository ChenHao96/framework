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

package org.steven.chen.component.netty;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.steven.chen.component.socket.connect.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

public class NettyServerConnectionContext extends DefaultConnectionContext implements ConnectionContext {

    private int connectionPort;
    private String connectionIp;
    private ChannelHandlerContext channelHandlerContext;
    private List<ConnectionCloseProcess> closeProcesses;
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerConnectionContext.class);

    public NettyServerConnectionContext(ChannelHandlerContext channelHandlerContext) {
        Assert.notNull(channelHandlerContext, "ChannelHandlerContext context is required!");
        this.channelHandlerContext = channelHandlerContext;
        InetSocketAddress socketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        this.connectionPort = socketAddress.getPort();
        this.connectionIp = socketAddress.getHostName();
    }

    public void setMessageConvertToHandlerArgs(MessageConvertToHandlerArgs messageConvertToHandlerArgs) {
        this.messageConvertToHandlerArgs = messageConvertToHandlerArgs;
    }

    @Override
    public boolean isClose() {
        return !channelHandlerContext.channel().isOpen();
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
        channelHandlerContext.channel().close();
        channelHandlerContext.close();
    }

    @Override
    public void sendMessage(CommonsMessage message) {
        try {
            channelHandlerContext.writeAndFlush(message);
        } catch (Exception e) {
            LOGGER.warn("sendMessage", e);
        }
    }

    @Override
    public void addCloseProcess(ConnectionCloseProcess process) {
        if (process == null) return;
        if (closeProcesses == null) {
            closeProcesses = new LinkedList<>();
        }
        closeProcesses.add(process);
    }

    @Override
    public void sendMessage(Object message) {
        if (isClose()) return;
        Assert.notNull(messageConvertToHandlerArgs, "MessageConvertToHandlerArgs is required!");
        sendMessage(messageConvertToHandlerArgs.convertMessageReturn(message));
    }

    @Override
    public String getConnectionIp() {
        return connectionIp;
    }

    @Override
    public int getConnectionPort() {
        return connectionPort;
    }
}
