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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.steven.chen.connect.CommonsMessage;
import org.steven.chen.connect.DefaultMessageConvertToHandlerArgs;
import org.steven.chen.utils.JsonUtils;
import org.steven.chen.utils.StringUtil;
import org.steven.chen.utils.mapper.JsonNode2FlatMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebSocketMessageConvertToHandlerArgs extends DefaultMessageConvertToHandlerArgs {

    @Override
    public Map<String, Object> convertArgs() {

        CommonsMessage message = getCommonsMessage();
        String requestBody = new String(message.getData(), StandardCharsets.UTF_8);
        object2FlatMapper = object2FlatMapper == null ? new JsonNode2FlatMapper() : object2FlatMapper;
        try {
            JsonNode jsonNode = JsonUtils.jsonStr2JsonNode(requestBody);
            return object2FlatMapper.toFlatMapper(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommonsMessage convertMessageReturn(Object obj) {
        CommonsMessage message = getCommonsMessage();
        message.setData(null);
        try {
            String responseBody = JsonUtils.object2Json(obj);
            if (StringUtil.isNotEmpty(responseBody)) {
                message.setData(responseBody.getBytes(StandardCharsets.UTF_8));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return message;
    }
}
