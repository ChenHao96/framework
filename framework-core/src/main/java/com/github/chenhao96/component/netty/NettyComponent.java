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
import com.github.chenhao96.component.net.DefaultMessageConvertToHandlerArgs;
import com.github.chenhao96.component.net.MessageConvertToHandlerArgs;
import com.github.chenhao96.component.process.handler.HandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class NettyComponent extends AbstractNettyComponent {

    private static final String COMPONENT_NAME = "NettyComponent";

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
    protected void channelInitializer(ServerBootstrap bootstrap) {
        if (bootstrap == null) return;
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast("codec", new NettyCommonCodecFactory());
                NettySocketFrameHandler socketFrameHandler = new NettySocketFrameHandler();
                socketFrameHandler.setHandlerFactory(handlerFactory);
                socketFrameHandler.setExecutorService(executorService);
                MessageConvertToHandlerArgs convertToHandlerArgs = messageConvertToHandlerArgs;
                if (convertToHandlerArgs == null) convertToHandlerArgs = new DefaultMessageConvertToHandlerArgs();
                socketFrameHandler.setMessageConvertToHandlerArgs(convertToHandlerArgs);
                socketChannel.pipeline().addLast("handler", socketFrameHandler);
            }
        }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
    }
}
