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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.chenhao96.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

    public static final String CLIENT_IP_KEY = "AbstractController_CLIENT_IP";
    public static final String CLIENT_PORT_KEY = "AbstractController_CLIENT_PORT";
    private static final ThreadLocal<HttpServletRequest> thread_request = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> thread_response = new ThreadLocal<>();

    @ModelAttribute
    protected void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        thread_request.set(request);
        thread_response.set(response);
    }

    protected HttpServletRequest getRequest() {
        return thread_request.get();
    }

    protected HttpServletResponse getResponse() {
        return thread_response.get();
    }

    protected HttpSession getSession() {
        return getRequest().getSession();
    }

    protected void renderResponseForJson(String contentType, String responseContent) throws IOException {
        HttpServletResponse response = this.getResponse();
        response.setContentType(contentType);
        response.getWriter().write(responseContent);
        response.getWriter().flush();
    }

    protected String getRequestBody() throws IOException {
        return RequestParamUtil.readRequestContent(getRequest());
    }

    protected String getRequestClientIP() {
        return (String) getRequest().getAttribute(CLIENT_IP_KEY);
    }

    protected int getRequestClientPort() {
        return (int) getRequest().getAttribute(CLIENT_PORT_KEY);
    }

    protected void responseFile(String contentType, String filePath) throws IOException {
        HttpServletResponse response = getResponse();
        File file = new File(filePath);
        if (!IOUtils.checkFile(file)) response.setStatus(404);
        response.setContentType(contentType);
        FileInputStream inputStream = new FileInputStream(file);
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            IOUtils.write(inputStream, outputStream);
        } finally {
            CommonsUtil.safeClose(inputStream);
            CommonsUtil.safeClose(outputStream);
        }
    }

    protected Map<String, String> param2MapStrParamJson() {
        try {
            String jsonParam = RequestParamUtil.readRequestContent(getRequest());
            LOGGER.info("param2MapStrParamJson :{}", jsonParam);
            if (StringUtil.isEmpty(jsonParam)) return null;
            JsonNode jsonNode = JsonUtils.jsonStr2JsonNodeStatic(jsonParam);
            return BeanMapConvertUtil.bean2StringMap(jsonNode);
        } catch (IOException e) {
            LOGGER.warn("param2MapStrParamJson", e);
        }
        return null;
    }

    protected Map<String, String> param2MapStrParam() {
        Map<String, String> params = RequestParamUtil.getRequestParam(getRequest());
        if (CollectionUtils.isEmpty(params)) {
            String contentType = getRequest().getContentType();
            if (StringUtil.isNotEmpty(contentType)) {
                if (contentType.toLowerCase().contains("json")) {
                    params = param2MapStrParamJson();
                } else if (contentType.toLowerCase().contains("xml")) {
                    params = param2MapStrParamXml();
                }
            }
        }
        return params;
    }

    protected Map<String, String> param2MapStrParamXml() {
        try {
            String xmlParam = RequestParamUtil.readRequestContent(getRequest());
            LOGGER.info("param2MapStrParamXml :{}", xmlParam);
            if (StringUtil.isEmpty(xmlParam)) return null;
            return XmlParserUtils.xmlToMap(xmlParam);
        } catch (Exception e) {
            LOGGER.warn("param2MapStrParamXml", e);
        }
        return null;
    }
}
