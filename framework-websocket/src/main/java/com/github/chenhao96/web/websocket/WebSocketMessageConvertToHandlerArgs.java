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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.chenhao96.component.net.CommonsMessage;
import com.github.chenhao96.component.net.DefaultMessageConvertToHandlerArgs;
import com.github.chenhao96.utils.JsonUtils;
import com.github.chenhao96.utils.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebSocketMessageConvertToHandlerArgs extends DefaultMessageConvertToHandlerArgs {

    @Override
    public Map<String, Object> convertArgs() {
        CommonsMessage message = getCommonsMessage();
        if (message == null) return null;
        String requestBody = new String(message.getData(), StandardCharsets.UTF_8);
        try {
            JsonNode jsonNode = JsonUtils.jsonStr2JsonNodeStatic(requestBody);
            return object2FlatMapper.toFlatMapper(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommonsMessage convertMessageReturn(Object obj) {
        CommonsMessage message = getCommonsMessage(true);
        message.setData(null);
        if (obj != null) {
            try {
                String responseBody = JsonUtils.object2JsonStatic(obj);
                if (StringUtil.isNotEmpty(responseBody)) {
                    message.setData(responseBody.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return message;
    }
}
