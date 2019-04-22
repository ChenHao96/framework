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

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.steven.chen.component.executor.TaskExecutorService;
import org.steven.chen.component.process.ProcessInvokeService;
import org.steven.chen.component.process.handler.HandlerFactory;
import org.steven.chen.component.socket.connect.CommonsMessage;
import org.steven.chen.component.socket.connect.MessageConvertToHandlerArgs;
import org.steven.chen.component.socket.connect.SocketHandlerTask;
import org.steven.chen.utils.JsonUtils;
import org.steven.chen.utils.encrypt.MD5Utils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

public class WebSocket extends TextWebSocketHandler {

    @Resource
    private HandlerFactory handlerFactory;

    @Resource
    private TaskExecutorService executorService;

    @Resource
    private MessageConvertToHandlerArgs messageConvertToHandlerArgs;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String requestStr = message.getPayload();
        JsonNode jsonNode = JsonUtils.jsonStr2JsonNode(requestStr);
        if (jsonNode != null && jsonNode.has(CommonsMessage.MASTER_CODE_NAME) && jsonNode.has(CommonsMessage.SLAVE_CODE_NAME)) {
            byte slaveCode = (byte) jsonNode.get(CommonsMessage.SLAVE_CODE_NAME).asInt();
            byte masterCode = (byte) jsonNode.get(CommonsMessage.MASTER_CODE_NAME).asInt();
            if (jsonNode.has(CommonsMessage.DATA_NAME)) {
                String dataBody = jsonNode.get(CommonsMessage.DATA_NAME).asText();
                if (jsonNode.has(CommonsMessage.CHECK_CODE_NAME)) {
                    if (MD5Utils.isEquals(dataBody, jsonNode.get(CommonsMessage.CHECK_CODE_NAME).asText())) {
                        CommonsMessage request = new CommonsMessage();
                        request.setSlaveCode(slaveCode);
                        request.setMasterCode(masterCode);
                        request.setData(dataBody.getBytes(StandardCharsets.UTF_8));
                        ProcessInvokeService invokeService = handlerFactory.getProcessMethod(request.getMasterCode(), request.getSlaveCode());
                        if (invokeService != null) {
                            SocketHandlerTask task = new SocketHandlerTask(request);
                            WebSocketFrameHandler frameHandler = new WebSocketFrameHandler(session);
                            frameHandler.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
                            task.setConnectionContext(frameHandler);
                            task.setInvokeService(invokeService);
                            task.setMessageConvertToHandlerArgs(messageConvertToHandlerArgs);
                            executorService.addHandler(task);
                        }
                    }
                }
            }
        }
    }
}
