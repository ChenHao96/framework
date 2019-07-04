package org.steven.chen.utils;

import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public final class RequestParamUtil {

    public static Map<String, String> getRequestParam(Map<String, String[]> reqMap) {
        if (CollectionUtils.isEmpty(reqMap)) return null;
        Map<String, String> resultMap = new HashMap<>(reqMap.size());
        for (Map.Entry<String, String[]> entry : reqMap.entrySet()) {
            String[] values = entry.getValue();
            if (values == null || values.length == 0) continue;
            resultMap.put(entry.getKey(), values[0]);
        }
        return resultMap;
    }

    public static Map<String, String> getRequestParamArray(Map<String, String[]> reqMap) {
        return getRequestParamArray(reqMap, null);
    }

    public static Map<String, String> getRequestParamArray(Map<String, String[]> reqMap, String charSet) {
        if (CollectionUtils.isEmpty(reqMap)) return null;
        Map<String, String> resultMap = new HashMap<>(reqMap.size());
        for (Map.Entry<String, String[]> entry : reqMap.entrySet()) {
            String[] values = entry.getValue();
            if (values == null || values.length == 0) continue;
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            if (StringUtil.isNotEmpty(charSet)) {
                try {
                    valueStr = new String(valueStr.getBytes(charSet), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            resultMap.put(entry.getKey(), valueStr);
        }
        return resultMap;
    }

    public static Map<String, String> getRequestParam(HttpServletRequest request) {
        if (request == null) return null;
        return getRequestParam(request.getParameterMap());
    }

    public static Map<String, String> getRequestParamArray(HttpServletRequest request) {
        if (request == null) return null;
        return getRequestParamArray(request.getParameterMap());
    }

    public static String readRequestContent(HttpServletRequest request) throws IOException {
        if (request == null) return null;
        BufferedReader bufferedReader = request.getReader();
        return IOUtils.read(bufferedReader);
    }
}
