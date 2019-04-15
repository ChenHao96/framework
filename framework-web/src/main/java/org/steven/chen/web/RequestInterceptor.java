package org.steven.chen.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.steven.chen.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RequestInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Boolean> httpRequest = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestInterceptor.class);

    public static boolean isHttpRequest() {
        return httpRequest.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        httpRequest.set(true);
        String requestUrl = request.getRequestURI();
        String address = Utils.getIPAndPort(request);
        String param = catalinaMap2String(request.getParameterMap());
        LOGGER.info("request address:{},requestUrl:{},param:{}", address, requestUrl, param);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        httpRequest.set(false);
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
}
