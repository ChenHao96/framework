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

import org.steven.chen.utils.Utils;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@WebListener
public class MyServletListener implements ServletRequestListener {

    public static final String REMOTE_CLIENT_IP_KEY = "REMOTE_CLIENT_IP_KEY";
    public static final String REMOTE_CLIENT_PORT_KEY = "REMOTE_CLIENT_PORT_KEY";

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {

    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
        if (request != null) {
            HttpSession session = request.getSession();
            session.setAttribute(REMOTE_CLIENT_PORT_KEY, request.getRemotePort());
            session.setAttribute(REMOTE_CLIENT_IP_KEY, Utils.getIp(request));
        }
    }
}