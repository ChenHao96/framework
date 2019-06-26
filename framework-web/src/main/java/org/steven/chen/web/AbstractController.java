package org.steven.chen.web;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.steven.chen.utils.CommonsUtil;
import org.steven.chen.utils.IOUtils;
import org.steven.chen.utils.RequestParamUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class AbstractController {

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
        return getRequest().getRemotePort();
    }

    protected void responseFile(String contentType, String filePath) throws IOException {
        HttpServletResponse response = getResponse();
        File file = new File(filePath);
        if (IOUtils.checkFile(file)) {
            response.setContentType(contentType);
            FileReader reader = new FileReader(file);
            PrintWriter writer = response.getWriter();
            IOUtils.write(reader, writer);
            CommonsUtil.safeClose(reader);
            CommonsUtil.safeClose(writer);
        } else {
            response.setStatus(404);
        }
    }
}
