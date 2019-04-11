package org.steven.chen.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public final class URLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLUtils.class);

    private URLUtils() {
    }

    public static String currentServerUrl(HttpServletRequest request) {

        if (request == null) return "";

        int port = request.getServerPort();
        String protocol = request.getScheme();
        String serverName = request.getServerName();
        String contextPath = request.getContextPath();

        return URLUtils.newUrl4Param(port, protocol, serverName, contextPath).toString();
    }

    private static StringBuilder newUrl4Param(int port, String protocol, String serverName, String contextPath) {

        StringBuilder sb = new StringBuilder(protocol);
        sb.append("://").append(serverName);

        if (port != -1) {
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

        if (StringUtils.isNotEmpty(contextPath)) {
            sb.append(contextPath);
        }

        LOGGER.info("newUrl4Param:{}",sb);
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

        sb.append("?");
        String paramStr = map2ParamStr(param);
        if (StringUtils.isNotEmpty(paramStr)) {
            sb.append(paramStr);
        }

        String query = uri.getRawQuery();
        if (StringUtils.isNotEmpty(query)) {
            sb.append(query);
        } else {
            sb.append("_");
        }

        String fragment = uri.getFragment();
        if (StringUtils.isNotBlank(newFragment)) {
            sb.append("#").append(newFragment);
        } else if (StringUtils.isNotEmpty(fragment)) {
            sb.append("#").append(fragment);
        }

        LOGGER.info("updateUrl:{}",sb);
        return sb.toString();
    }

    private static String map2ParamStr(Map<String, String> params) {

        if (params != null && params.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (String key : params.keySet()) {
                if (StringUtils.isNotBlank(key)) {
                    String value = params.get(key);
                    if (StringUtils.isEmpty(value)) {
                        value = "null";
                    }
                    value = value.trim().replaceAll(" ", "%20").replaceAll("\t", "");
                    builder.append(key).append("=").append(value).append("&");
                }
            }

            return builder.toString();
        }

        return "";
    }
}
