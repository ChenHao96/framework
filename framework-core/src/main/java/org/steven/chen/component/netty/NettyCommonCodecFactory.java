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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.steven.chen.component.net.CommonsMessage;

import java.nio.ByteBuffer;
import java.util.List;

public class NettyCommonCodecFactory extends ByteToMessageCodec<CommonsMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyCommonCodecFactory.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, CommonsMessage commonsMessage, ByteBuf byteBuf) throws Exception {
        if (commonsMessage == null) return;
        ByteBuffer byteBuffer = CommonsMessage.createByteBufByMessage(commonsMessage);
        byteBuf.writeBytes(byteBuffer);
        channelHandlerContext.flush();
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        try {
            CommonsMessage message = CommonsMessage.createMessageByByteBuf(byteBuf);
            list.add(message);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("decode", e);
        }
    }
}
