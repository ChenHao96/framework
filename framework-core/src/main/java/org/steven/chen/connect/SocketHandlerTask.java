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

package org.steven.chen.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.steven.chen.component.process.ProcessInvokeService;
import org.steven.chen.utils.CommonsUtil;

import java.net.SocketException;
import java.util.Map;

public class SocketHandlerTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandlerTask.class);

    private CommonsMessage request;
    private ProcessInvokeService invokeService;
    private ConnectionContext connectionContext;
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;

    public SocketHandlerTask(CommonsMessage request) {
        this.request = request;
    }

    public void setRequest(CommonsMessage request) {
        this.request = request;
    }

    public void setInvokeService(ProcessInvokeService invokeService) {
        this.invokeService = invokeService;
    }

    public void setConnectionContext(ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    public void setMessageConvertToHandlerArgs(MessageConvertToHandlerArgs messageConvertToHandlerArgs) {
        this.messageConvertToHandlerArgs = messageConvertToHandlerArgs;
    }

    @Override
    public void run() {
        Assert.notNull(request, "request body is required!");
        Assert.notNull(messageConvertToHandlerArgs, "MessageConvertToHandlerArgs is required!");
        messageConvertToHandlerArgs.setCommonsMessage(request);
        SocketConnectionUtil.setChannelHandlerContext(connectionContext);

        final Map<String, Object> param = messageConvertToHandlerArgs.convertArgs();
        LOGGER.info("request masterCode:{},slaveCode:{},param:{}", request.getMasterCode(), request.getSlaveCode(), param);
        try {
            Object returnValue = invokeService.invokeProcess(param);
            if (!invokeService.isReturnVoid()) {
                CommonsMessage response = messageConvertToHandlerArgs.convertMessageReturn(returnValue);
                connectionContext.sendMessage(response);
            }
        } catch (SocketException close) {
            CommonsUtil.safeClose(connectionContext);
        } catch (Exception e) {
            LOGGER.warn("invokeProcess fail. param:{}", param, e);
        } finally {
            messageConvertToHandlerArgs.removeCommonsMessage();
            SocketConnectionUtil.removeChannelHandlerContext();
        }
    }
}
