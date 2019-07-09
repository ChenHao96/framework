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

package com.github.chenhao96.component.netty;

import com.github.chenhao96.component.executor.TaskExecutorService;
import com.github.chenhao96.component.net.CommonsMessage;
import com.github.chenhao96.component.net.MessageConvertToHandlerArgs;
import com.github.chenhao96.component.net.SocketHandlerTask;
import com.github.chenhao96.component.process.ProcessInvokeService;
import com.github.chenhao96.component.process.handler.HandlerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettySocketFrameHandler extends SimpleChannelInboundHandler<CommonsMessage> {

    private HandlerFactory handlerFactory;

    private TaskExecutorService executorService;

    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;

    public void setHandlerFactory(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    public void setExecutorService(TaskExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setMessageConvertToHandlerArgs(MessageConvertToHandlerArgs messageConvertToHandlerArgs) {
        this.messageConvertToHandlerArgs = messageConvertToHandlerArgs;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CommonsMessage commonsMessage) throws Exception {
        if (commonsMessage == null) return;
        ProcessInvokeService invokeService = handlerFactory.getProcessMethod(commonsMessage.getMasterCode(), commonsMessage.getSlaveCode());
        if (invokeService != null) {
            SocketHandlerTask task = new SocketHandlerTask(commonsMessage);
            NettyServerConnectionContext connectionContext = new NettyServerConnectionContext(channelHandlerContext);
            connectionContext.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
            task.setConnectionContext(connectionContext);
            task.setInvokeService(invokeService);
            task.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
            executorService.addHandler(task);
        }
    }
}
