package org.steven.chen.web;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.steven.chen.utils.CommonsUtil;
import org.steven.chen.utils.StringUtil;
import org.steven.chen.utils.URLUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

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
        return receiveRequestInput();
    }

    protected String getRequestClientIP() {
        return (String) getRequest().getAttribute(CLIENT_IP_KEY);
    }

    protected int getRequestClientPort() {
        return getRequest().getRemotePort();
    }

    protected String createCurrentContextUrl(String url) {
        HttpServletRequest request = getRequest();
        int port = request.getServerPort();
        String protocol = request.getScheme();
        String serverName = request.getServerName();
        String contextPath = request.getContextPath();
        StringBuilder sb = URLUtils.newUrl4Param(port, protocol, serverName, contextPath);
        if (StringUtil.isNotBlank(url)) {
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
        }
        return sb.toString();
    }

    private String receiveRequestInput() throws IOException {

        int cnt;
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();

        InputStream inputStream = getRequest().getInputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        while ((cnt = bis.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, cnt));
        }

        CommonsUtil.safeClose(bis, inputStream);
        return sb.toString();
    }

    protected void responseFile(String fileType, File file) {

        HttpServletResponse response = getResponse();
        if (file == null || StringUtil.isEmpty(fileType)) return;

        try {
            response.setContentType(fileType);
            FileInputStream fis = new FileInputStream(file);
            OutputStream ops = response.getOutputStream();

            int count;
            byte[] buffer = new byte[1024 * 1024];
            while ((count = fis.read(buffer)) != -1) {
                ops.write(buffer, 0, count);
                ops.flush();
            }

            CommonsUtil.safeClose(ops, fis);
        } catch (FileNotFoundException e) {
            response.setStatus(404);
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}
