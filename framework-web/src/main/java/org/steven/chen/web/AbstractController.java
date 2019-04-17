package org.steven.chen.web;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.handler.InvocableHandlerMethod;
import org.steven.chen.component.socket.connect.SocketConnectionUtil;
import org.steven.chen.utils.CommonsUtil;
import org.steven.chen.utils.StringUtil;
import org.steven.chen.utils.URLUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Map;

public abstract class AbstractController implements ProcessHandlerService {

    public static final String CLIENT_IP_KEY = "AbstractController_CLIENT_IP";
    public static final String CLIENT_PORT_KEY = "AbstractController_CLIENT_PORT";
    private static final ThreadLocal<HttpServletRequest> thread_request = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> thread_response = new ThreadLocal<>();

    @ModelAttribute
    protected void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        thread_request.set(request);
        thread_response.set(response);
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

    protected Object getRequestParameter(String name) {
        if (RequestInterceptor.isHttpRequest()) {
            return getRequest().getParameter(name);
        } else {
            Map<String, Object> param = InvocableHandlerMethod.getProcessParam();
            return param == null ? null : param.get(name);
        }
    }

    protected Map<String, ?> getRequestParameterMap() {
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

    protected void setResponseStatus(int code) {
        if (RequestInterceptor.isHttpRequest()) {
            getResponse().setStatus(code);
        }
    }

    protected String getRequestBody() throws IOException {
        if (!RequestInterceptor.isHttpRequest()) return "";
        return receiveRequestInput();
    }

    protected void requestDispatcherForward(String path) throws ServletException, IOException {
        if (RequestInterceptor.isHttpRequest()) {
            getRequest().getRequestDispatcher(path).forward(getRequest(), getResponse());
        }
    }

    protected void sendRedirect(String path) throws IOException {
        if (RequestInterceptor.isHttpRequest()) {
            getResponse().sendRedirect(path);
        }
    }

    protected void responseFile(String fileType, File file) {
        if (RequestInterceptor.isHttpRequest()) {
            responseImage(fileType, file);
        }
    }

    protected String getRequestClientIP() {
        if (RequestInterceptor.isHttpRequest()) {
            return (String) getRequest().getAttribute(CLIENT_IP_KEY);
        } else {
            return SocketConnectionUtil.getChannelHandlerContext().getConnectionIp();
        }
    }

    protected int getRequestClientPort() {
        if (RequestInterceptor.isHttpRequest()) {
            return getRequest().getRemotePort();
        } else {
            return SocketConnectionUtil.getChannelHandlerContext().getConnectionPort();
        }
    }

    protected String createCurrentContextUrl(String url) {
        if (!RequestInterceptor.isHttpRequest()) return url;
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

    private void responseImage(String fileType, File file) {

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
