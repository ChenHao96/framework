package com.github.chenhao96.web;

import com.github.chenhao96.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RequestInterceptor implements HandlerInterceptor {

    private static final String CALLBACK_HTTP_PARAMETER_NAME = "callback";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getIp(request);
        int port = request.getRemotePort();
        String requestUrl = request.getRequestURI();
        request.setAttribute(AbstractController.CLIENT_IP_KEY, ip);
        String param = catalinaMap2String(request.getParameterMap());
        request.setAttribute(AbstractController.CLIENT_PORT_KEY, port);
        String callBackName = request.getParameter(CALLBACK_HTTP_PARAMETER_NAME);
        if (StringUtil.isNotEmpty(callBackName)) MappingJackson2HttpMessageConverter.setJsonPCallBackName(callBackName);
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
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (StringUtils.isEmpty(ip) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)) {
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
