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
package com.github.chenhao96.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class URLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLUtils.class);

    public static String createCallBackUrl(String url, HttpServletRequest request) {

        if (request == null) return null;

        StringBuilder sb = new StringBuilder(currentServerUrl(request));
        if (StringUtils.isNotBlank(url)) {
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
        }

        String callBackUrl = sb.toString();
        LOGGER.info("createCallBackUrl callBackUrl:{}", callBackUrl);
        return callBackUrl;
    }

    public static String currentServerUrl(HttpServletRequest request) {
        if (request == null) return "";
        int port = request.getServerPort();
        String protocol = request.getScheme();
        String serverName = request.getServerName();
        String contextPath = request.getContextPath();
        return newUrl4Param(port, protocol, serverName, contextPath).toString();
    }

    private static StringBuilder newUrl4Param(int port, String protocol, String serverName, String contextPath) {
        StringBuilder sb = new StringBuilder(protocol);
        sb.append("://").append(serverName);
        if (port > 0 && port < 65536) {
            if ("http".equals(protocol)) {
                if (port != 80) {
                    sb.append(":").append(port);
                }
            } else if ("https".equals(protocol)) {
                if (port != 443) {
                    sb.append(":").append(port);
                }
            }
        }
        if (StringUtils.isNotEmpty(contextPath)) sb.append(contextPath);
        LOGGER.info("newUrl4Param:{}", sb);
        return sb;
    }

    public static String updateUrl(String url, Map<String, String> param, String newFragment) {

        if (StringUtils.isBlank(url)) return "";

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        int port = uri.getPort();
        String protocol = uri.getScheme();
        String serverName = uri.getHost();
        String contextPath = uri.getPath();
        StringBuilder sb = newUrl4Param(port, protocol, serverName, contextPath);

        param = updateRawQueryParam(uri.getRawQuery(), param);
        String paramStr = paramToQueryString(param);
        if (StringUtils.isNotEmpty(paramStr)) {
            sb.append(paramStr);
        }

        String fragment = uri.getFragment();
        if (StringUtils.isNotBlank(newFragment)) {
            sb.append("#").append(newFragment);
        } else if (StringUtils.isNotEmpty(fragment)) {
            sb.append("#").append(fragment);
        }

        LOGGER.info("updateUrl:{}", sb);
        return sb.toString();
    }

    private static Map<String, String> updateRawQueryParam(String rawQuery, Map<String, String> param) {
        Map<String, String> queryMap = getQueryMap(rawQuery);
        int paramCount = queryMap == null ? 0 : queryMap.size();
        paramCount += param == null ? 0 : param.size();
        if (paramCount == 0) return null;
        Map<String, String> result = new HashMap<>(paramCount);
        putNewMap(param, result);
        putNewMap(queryMap, result);
        return result;
    }

    public static void putNewMap(Map<String, String> param, Map<String, String> result) {
        if (param != null) {
            Set<Map.Entry<String, String>> entrySet = param.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                if (StringUtil.isEmpty(key)) continue;
                result.put(key, urlEncode(entry.getValue()));
            }
        }
    }

    public static String paramToQueryString(Map<String, String> params, boolean encode) {
        if (CollectionUtils.isEmpty(params)) return "";
        StringBuilder paramString = new StringBuilder();
        boolean noFirst = false;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (StringUtil.isBlank(key)) continue;
            String value = entry.getValue();
            if (StringUtil.isNotBlank(value)) {
                if (encode) value = urlEncode(value);
                if (noFirst) paramString.append("&");
                paramString.append(key).append("=").append(value);
                noFirst = true;
            }
        }
        return paramString.toString();
    }

    public static Map<String, String> getQueryMap(String rawQuery) {

        String[] pas = null;
        if (StringUtils.isNotEmpty(rawQuery)) pas = rawQuery.split("&");

        Map<String, String> result = null;
        if (pas != null) {
            result = new HashMap<>(pas.length);
            for (String str : pas) {
                if (str == null) continue;
                String[] kv = str.split("=");
                if (kv.length == 2) {
                    String key = kv[0];
                    String value = kv[1];
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    public static String urlEncode(String value) {
        if (StringUtil.isEmpty(value)) return value;
        //TODO:需要判断是否已经encode了
        try {
            String result = URLEncoder.encode(value, CommonsUtil.SYSTEM_ENCODING);
            result = result.trim().replaceAll("\\+", "%20").replaceAll("\\*", "%2A").replaceAll("~", "%7E");
            result = result.replaceAll("/", "%2F").replaceAll(" ", "%20").replaceAll("\\t", "");
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String paramToQueryString(Map<String, String> params) {
        return paramToQueryString(params, true);
    }
}
