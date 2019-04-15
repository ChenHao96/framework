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

package org.steven.chen.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.steven.chen.utils.JsonUtils;
import org.steven.chen.utils.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BaseController extends AbstractController {

    public static final String DATA_JSON_PROPERTY = "data";
    private static final String LOG_ID_JSON_PROPERTY = "logId";
    private static final String URL_JSON_PROPERTY = "url";
    public static final String STATUS_JSON_PROPERTY = "result";
    public static final String MESSAGE_JSON_PROPERTY = "message";
    public static final String CODE_JSON_PROPERTY = "error_code";
    private static final String TEMPLATE_JSON_PROPERTY = "template";
    public static final String CALLBACK_RESPONSE_CONTENT_FMT = "%s(%s);";
    public static final String CALLBACK_HTTP_PARAMETER_NAME = "callback";
    public static final String CONTENT_TYPE_HTTP_HEADER = "application/json;charset=utf-8";

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
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, template);

        try {
            String e = fmtJsonPString(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var9) {
            throw new RuntimeException(var9.getMessage(), var9);
        }
    }

    protected void responseJsonp(boolean result, int responseCode, String message, Object returnData, String url) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, url, null);

        try {
            String e = fmtJsonPString(jsonParamsMap);
            this.renderResponseForJson(e);
        } catch (Exception var8) {
            throw new RuntimeException(var8.getMessage(), var8);
        }
    }

    protected void responseJsonp(boolean result, int responseCode, String message, Object returnData) {
        Map jsonParamsMap = this.generateJsonParamsMap(result, responseCode, message, returnData, null, null, null);

        try {
            String e = fmtJsonPString(jsonParamsMap);
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

    private String fmtJsonPString(Object jsonParam) throws JsonProcessingException {
        String callBackName = (String) getRequestParameter(CALLBACK_HTTP_PARAMETER_NAME);
        String fmt = CALLBACK_RESPONSE_CONTENT_FMT;
        if (StringUtil.isEmpty(callBackName)) {
            fmt = "%s%s";
            callBackName = "";
        }
        return String.format(fmt, callBackName, JsonUtils.object2Json(jsonParam));
    }
}
