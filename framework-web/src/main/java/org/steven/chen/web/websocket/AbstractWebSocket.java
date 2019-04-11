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

import javax.websocket.*;

public abstract class AbstractWebSocket {

    public abstract int getSessionPort(Session session);

    public abstract String getSessionIP(Session session);

    @OnOpen
    public void onOpen(Session session) {

    }

    @OnMessage
    public void onMessage(String message, Session session) {

    }

    @OnClose
    public void onClose(Session session) {

    }

    @OnError
    public void onError(Session session, Throwable error) {

    }
}