/**
 * Copyright 2018 ChenHao96
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

import javax.websocket.Session;

//@ServerEndpoint(value = "/webSocket", configurator = MyConfigurator.class)
public class BaseWebSocketManager extends AbstractWebSocket {

    @Override
    public String getSessionIP(Session session) {
        return (String) session.getUserProperties().get(MyServletListener.REMOTE_CLIENT_IP_KEY);
    }

    @Override
    public int getSessionPort(Session session) {
        return (int) session.getUserProperties().get(MyServletListener.REMOTE_CLIENT_PORT_KEY);
    }
}