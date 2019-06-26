package org.steven.chen.utils;

import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
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

    public static Map<String, String> getRequestParam(HttpServletRequest request) {
        if (request == null) return null;
        return getRequestParam(request.getParameterMap());
    }

    public static String readRequestContent(HttpServletRequest request) throws IOException {
        if (request == null) return null;
        BufferedReader bufferedReader = request.getReader();
        return IOUtils.read(bufferedReader);
    }
}
