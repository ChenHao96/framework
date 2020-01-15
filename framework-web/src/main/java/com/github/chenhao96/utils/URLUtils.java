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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class URLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLUtils.class);

    public static String createCallBackUrl(String url, HttpServletRequest request) {

        if (request == null) return null;

        StringBuilder sb = new StringBuilder(currentServerUrl(request));
        if (StringUtil.isNotBlank(url)) {
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
        }

        String callBackUrl = sb.toString();
        LOGGER.info("createCallBackUrl callBackUrl:{}", callBackUrl);
        return callBackUrl;
    }

    public static String createCallBackUrl(String url, String serverUrl) {
        StringBuilder sb = new StringBuilder(serverUrl);
        if (StringUtil.isNotBlank(url)) {
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
        }

        String callBackUrl = sb.toString();
        LOGGER.info("createCallBackUrl callBackUrl:{}", callBackUrl);
        return callBackUrl;
    }

    public static String currentServerUrl(HttpServletRequest request, String defaultServerUrl) {
        if (StringUtil.isNotEmpty(defaultServerUrl)) return defaultServerUrl;
        if (request == null) return "";
        int port = request.getServerPort();
        String protocol = request.getScheme();
        String serverName = request.getServerName();
        String contextPath = request.getContextPath();
        return newUrl4Param(port, protocol, serverName, contextPath).toString();
    }


    public static String currentServerUrl(HttpServletRequest request) {
        return currentServerUrl(request, null);
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
        if (StringUtil.isNotEmpty(contextPath)) sb.append(contextPath);
        LOGGER.info("newUrl4Param:{}", sb);
        return sb;
    }

    public static String updateUrl(String url, Map<String, String> param, String newFragment) {
        return updateUrl(url, param, newFragment, CommonsUtil.DEFAULT_ENCODING);
    }

    public static String updateUrl(String url, Map<String, String> param, String newFragment, String charSet) {

        if (StringUtil.isBlank(url)) return "";

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

        String paramStr;
        try {
            param = updateRawQueryParam(uri.getRawQuery(), param, charSet);
            paramStr = paramToQueryString(param, charSet);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (StringUtil.isNotEmpty(paramStr)) {
            sb.append("?").append(paramStr);
        }

        String fragment = uri.getFragment();
        if (StringUtil.isNotBlank(newFragment)) {
            sb.append("#").append(newFragment);
        } else if (StringUtil.isNotEmpty(fragment)) {
            sb.append("#").append(fragment);
        }

        LOGGER.info("updateUrl:{}", sb);
        return sb.toString();
    }

    private static Map<String, String> updateRawQueryParam(String rawQuery, Map<String, String> param, String charSet) throws UnsupportedEncodingException {
        Map<String, String> queryMap = getQueryMap(rawQuery, charSet);
        int paramCount = queryMap == null ? 0 : queryMap.size();
        paramCount += param == null ? 0 : param.size();
        if (paramCount == 0) return null;
        Map<String, String> result = new HashMap<>(paramCount);
        putNewMap(param, result, charSet);
        putNewMap(queryMap, result, charSet);
        return result;
    }

    public static void putNewMap(Map<String, String> param, Map<String, String> result, String charSet) throws UnsupportedEncodingException {
        if (param != null) {
            Set<Map.Entry<String, String>> entrySet = param.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                if (StringUtil.isEmpty(key)) continue;
                String value = URLEncoder.encode(entry.getValue(), charSet);
                result.put(key, value);
            }
        }
    }

    public static String paramToQueryString(Map<String, String> params, String charSet) throws UnsupportedEncodingException {
        if (CollectionUtils.isEmpty(params)) return "";
        StringBuilder paramString = new StringBuilder();
        boolean noFirst = false;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (StringUtil.isBlank(key)) continue;
            String value = entry.getValue();
            if (StringUtil.isNotBlank(value)) {
                value = URLEncoder.encode(value, charSet);
                if (noFirst) paramString.append("&");
                paramString.append(key).append("=").append(value);
                noFirst = true;
            }
        }
        return paramString.toString();
    }

    public static Map<String, String> getQueryMap(String rawQuery, String charSet) throws UnsupportedEncodingException {

        String[] pas = null;
        if (StringUtil.isNotEmpty(rawQuery)) pas = rawQuery.split("&");

        Map<String, String> result = null;
        if (pas != null) {
            result = new HashMap<>(pas.length);
            for (String str : pas) {
                if (str == null) continue;
                String[] kv = str.split("=");
                if (kv.length == 2) {
                    String key = kv[0];
                    String value = kv[1];
                    if (StringUtil.isNotEmpty(value)) {
                        value = URLDecoder.decode(value, charSet);
                    }
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    public static String paramToQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        return paramToQueryString(params, CommonsUtil.DEFAULT_ENCODING);
    }
}
