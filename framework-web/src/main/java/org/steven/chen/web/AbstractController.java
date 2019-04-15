package org.steven.chen.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.handler.InvocableHandlerMethod;
import org.steven.chen.component.socket.connect.SocketConnectionUtil;
import org.steven.chen.utils.JsonUtils;
import org.steven.chen.utils.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractController implements ProcessHandlerService {

    public static final String DATA_JSON_PROPERTY = "data";
    private static final String LOG_ID_JSON_PROPERTY = "logId";
    private static final String URL_JSON_PROPERTY = "url";
    public static final String STATUS_JSON_PROPERTY = "result";
    public static final String MESSAGE_JSON_PROPERTY = "message";
    public static final String CODE_JSON_PROPERTY = "error_code";
    private static final String TEMPLATE_JSON_PROPERTY = "template";
    public static final String CALLBACK_HTTP_PARAMETER_NAME = "callback";
    public static final String CONTENT_TYPE_HTTP_HEADER = "application/json;charset=utf-8";
    public static final String CALLBACK_RESPONSE_CONTENT_FMT = "%s(%s);";
    private static final ThreadLocal<HttpServletRequest> thread_request = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> thread_response = new ThreadLocal<>();

    @ModelAttribute
    protected void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        thread_request.set(request);
        thread_response.set(response);
    }

    protected void responseJsonUseStringValue(boolean result, int responseCode, String message, Object returnData, String url, String template) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, template);

        try {
            String e = JsonUtils.object2JsonUseStringValue(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var8) {
            throw new RuntimeException(var8.getMessage(), var8);
        }
    }

    protected void responseJsonUseStringValue(boolean result, int responseCode, String message, Object returnData, String url) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, null);

        try {
            String e = JsonUtils.object2JsonUseStringValue(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var7) {
            throw new RuntimeException(var7.getMessage(), var7);
        }
    }

    protected void responseJsonUseStringValue(boolean result, int responseCode, String message, Object returnData) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, null, null);

        try {
            String e = JsonUtils.object2JsonUseStringValue(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var6) {
            throw new RuntimeException(var6.getMessage(), var6);
        }
    }

    protected void responseJson(boolean result, int responseCode, String message, Object returnData, String url, String template) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, template);

        try {
            String e = JsonUtils.object2Json(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var8) {
            throw new RuntimeException(var8.getMessage(), var8);
        }
    }

    protected void responseJson(boolean result, int responseCode, String message, Object returnData, String url) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, null);

        try {
            String e = JsonUtils.object2Json(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var7) {
            throw new RuntimeException(var7.getMessage(), var7);
        }
    }

    protected void responseJson(boolean result, int responseCode, String message, Object returnData) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, null, null);

        try {
            String e = JsonUtils.object2Json(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var6) {
            throw new RuntimeException(var6.getMessage(), var6);
        }
    }

    protected void responseJsonp(boolean result, int responseCode, String message, Object returnData, String url, String template) {
        HttpServletRequest request = this.getRequest();
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, template);

        try {
            String e = fmtJsonPString(request, jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var9) {
            throw new RuntimeException(var9.getMessage(), var9);
        }
    }

    public static String fmtJsonPString(HttpServletRequest request, Object jsonParam) throws JsonProcessingException {
        String callBackName = request.getParameter(CALLBACK_HTTP_PARAMETER_NAME);
        String fmt = AbstractController.CALLBACK_RESPONSE_CONTENT_FMT;
        if (StringUtil.isEmpty(callBackName)) {
            fmt = "%s%s";
            callBackName = "";
        }
        return String.format(fmt, callBackName, JsonUtils.object2Json(jsonParam));
    }

    protected void responseJsonp(boolean result, int responseCode, String message, Object returnData, String url) {
        HttpServletRequest request = this.getRequest();
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, null);

        try {
            String e = fmtJsonPString(request, jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var8) {
            throw new RuntimeException(var8.getMessage(), var8);
        }
    }

    protected void responseJsonp(boolean result, int responseCode, String message, Object returnData) {
        HttpServletRequest request = this.getRequest();
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, null, null);

        try {
            String e = fmtJsonPString(request, jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var7) {
            throw new RuntimeException(var7.getMessage(), var7);
        }
    }

    protected void responseJsonUseStringValueByApi(boolean result, int responseCode, String message, Object returnData, String logId) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, logId, null, null);

        try {
            String e = JsonUtils.object2JsonUseStringValue(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var7) {
            throw new RuntimeException(var7.getMessage(), var7);
        }
    }

    protected void responseJsonByApi(boolean result, int responseCode, String message, Object returnData, String logId) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, logId, null, null);

        try {
            String e = JsonUtils.object2Json(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var7) {
            throw new RuntimeException(var7.getMessage(), var7);
        }
    }

    private Map<String, Object> generateJsonParamsMap(boolean result, int responseCode, String message, Object returnData, String logId, String url, String template) {

        HashMap<String, Object> jsonParamsMap = new HashMap<>(7);

        jsonParamsMap.put(STATUS_JSON_PROPERTY, result);
        jsonParamsMap.put(MESSAGE_JSON_PROPERTY, message);
        jsonParamsMap.put(CODE_JSON_PROPERTY, responseCode);
        jsonParamsMap.put(DATA_JSON_PROPERTY, returnData == null ? new int[0] : returnData);

        if (StringUtil.isNotBlank(url)) {
            jsonParamsMap.put(URL_JSON_PROPERTY, url);
        }

        if (StringUtil.isNotBlank(logId)) {
            jsonParamsMap.put(LOG_ID_JSON_PROPERTY, logId);
        }

        if (StringUtil.isNotBlank(template)) {
            jsonParamsMap.put(TEMPLATE_JSON_PROPERTY, template);
        }

        return jsonParamsMap;
    }

    public static String getCallbackHttpParameterName() {
        return CALLBACK_HTTP_PARAMETER_NAME;
    }

    protected void renderResponseForJson(String responseContent) throws IOException {
        renderResponseForJson(CONTENT_TYPE_HTTP_HEADER, responseContent);
    }

    private HttpServletRequest getRequest() {
        return thread_request.get();
    }

    private HttpServletResponse getResponse() {
        return thread_response.get();
    }

    private HttpSession getSession() {
        return getRequest().getSession();
    }

    protected void renderResponseForJson(String contentType, String responseContent) throws IOException {
        if (RequestInterceptor.isHttpRequest()) {
            HttpServletResponse response = this.getResponse();
            response.setContentType(contentType);
            response.getWriter().write(responseContent);
            response.getWriter().flush();
        } else {
            SocketConnectionUtil.getChannelHandlerContext().sendMessage(responseContent);
        }
    }

    protected Map<String, ?> getRequestParam() {
        if (RequestInterceptor.isHttpRequest()) {
            return getRequest().getParameterMap();
        } else {
            return InvocableHandlerMethod.getProcessParam();
        }
    }

    protected void setRequestAttribute(String key, Object obj) {
        if (RequestInterceptor.isHttpRequest()) {
            getRequest().setAttribute(key, obj);
        }
    }

    protected void setSessionAttribute(String key, Object obj) {
        if (RequestInterceptor.isHttpRequest()) {
            getSession().setAttribute(key, obj);
        } else {
            SocketConnectionUtil.getChannelHandlerContext().setAttribute(key, obj);
        }
    }

    protected void removeRequestAttribute(String key) {
        if (RequestInterceptor.isHttpRequest()) {
            getRequest().removeAttribute(key);
        }
    }

    protected void removeSessionAttribute(String key) {
        if (RequestInterceptor.isHttpRequest()) {
            getSession().removeAttribute(key);
        } else {
            SocketConnectionUtil.getChannelHandlerContext().removeAttribute(key);
        }
    }

    protected Object getRequestAttribute(String key) {
        if (RequestInterceptor.isHttpRequest()) {
            return getRequest().getAttribute(key);
        }
        return null;
    }

    protected Object getSessionAttribute(String key) {
        if (RequestInterceptor.isHttpRequest()) {
            return getSession().getAttribute(key);
        } else {
            return SocketConnectionUtil.getChannelHandlerContext().getAttribute(key);
        }
    }

    protected String getSessionId() {
        if (RequestInterceptor.isHttpRequest()) {
            return getSession().getId();
        } else {
            return SocketConnectionUtil.getChannelHandlerContext().Id();
        }
    }
}
