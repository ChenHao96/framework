package org.steven.chen.web;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.steven.chen.component.process.ProcessHandlerService;
import org.steven.chen.component.process.handler.InvocableHandlerMethod;
import org.steven.chen.component.socket.connect.SocketConnectionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractController implements ProcessHandlerService {

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

    //TODO:
}
