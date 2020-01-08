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
package com.github.chenhao96.web;

import com.github.chenhao96.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RequestInterceptor implements HandlerInterceptor {

    public static final String CALLBACK_HTTP_PARAMETER_NAME = "callback";
    public static final String SERVLET_PATH_PARAMETER_NAME = "servlet_path_key";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getIp(request);
        int port = request.getRemotePort();
        String requestUrl = request.getServletPath();
        if (StringUtil.isEmpty(requestUrl)) {
            requestUrl = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (contextPath != null && contextPath.length() > 0) {
                requestUrl = requestUrl.substring(contextPath.length());
            }
        }
        request.setAttribute(AbstractController.CLIENT_IP_KEY, ip);
        String param = catalinaMap2String(request.getParameterMap());
        request.setAttribute(SERVLET_PATH_PARAMETER_NAME, requestUrl);
        request.setAttribute(AbstractController.CLIENT_PORT_KEY, port);
        String callBackName = request.getParameter(CALLBACK_HTTP_PARAMETER_NAME);
        if (!StringUtil.isEmpty(callBackName)) MappingJackson2HttpMessageConverter.setJsonPCallBackName(callBackName);
        LOGGER.info("request address:{}:{},requestUrl:{},param:{}", ip, port, requestUrl, param);
        return true;
    }

    private String catalinaMap2String(Map map) {
        Set keys = map.keySet();
        StringBuilder msg = new StringBuilder("{");
        if (keys.size() > 0) {

            for (Object key : keys) {
                msg.append(",");
                msg.append(key);
                msg.append(":");
                msg.append(Arrays.toString((Object[]) map.get(key)));
            }

            if (msg.length() > 0) {
                msg.delete(1, 2);
            }
        }
        msg.append("}");
        return msg.toString();
    }

    public String getIp(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (StringUtil.isEmpty(ip) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)) {
            ip = "127.0.0.1";
        }

        if (ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }

        return ip;
    }
}
