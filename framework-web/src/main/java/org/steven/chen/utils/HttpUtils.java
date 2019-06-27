package org.steven.chen.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class HttpUtils {

    private static final String HEADER_KEY = "Set-Cookie";
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    public static String doGet(String url) {
        return doGet(url, null);
    }

    public static String doPost(String url) {
        return doPost(url, null);
    }

    public static String doGet(String url, Map<String, String> params) {
        return doGet(url, params, new Cookie[0]);
    }

    public static String doPost(String url, Map<String, String> params) {
        return doPost(url, params, new Cookie[0]);
    }

    public static String doGet(String url, Map<String, String> params, Cookie... cookies) {
        return doGet(url, params, null, cookies).getResponseBody();
    }

    public static String doPost(String url, Map<String, String> params, Cookie... cookies) {
        return doPost(url, params, null, cookies).getResponseBody();
    }

    public static String toGetUrl(String url, Map<String, String> params) {
        return addParams(url, params);
    }

    public static HttpResponse doService(CloseableHttpClient httpClient, HttpRequestBase requestBase, Map<String, String> headers, Cookie... cookies) {
        HttpResponse result = new HttpResponse();
        if (requestBase == null) return result;
        CloseableHttpResponse httpResponse = null;
        try {
            addCookies(requestBase, cookies);
            addHeaders(requestBase, headers);
            httpResponse = httpClient.execute(requestBase);
            byte[] responseByte = IOUtils.readStream2ByteArray(httpResponse.getEntity().getContent());
            result.setResponseByte(responseByte);
            Header contentType = httpResponse.getFirstHeader(CONTENT_TYPE_KEY);
            String contextTypeValue = contentType.getValue();
            int index = contextTypeValue.toUpperCase().indexOf("CHARSET");
            String charSet = contextTypeValue.substring(index);
            charSet = charSet.substring(charSet.indexOf("=") + 1).replace(";", "");
            result.setResponseBody(new String(responseByte, charSet));
            getCookies(result, httpResponse);
        } catch (Exception e) {
            LOGGER.warn("Http doService 异常 : {}", e.getMessage(), e);
        } finally {
            requestBase.releaseConnection();
            CommonsUtil.safeClose(httpResponse);
            CommonsUtil.safeClose(httpClient);
        }
        return result;
    }

    public static HttpResponse doGet(String url, Map<String, String> params, Map<String, String> headers, Cookie... cookies) {
        if (StringUtils.isEmpty(url)) throw new IllegalArgumentException("url为空");
        CloseableHttpClient httpClient = createHttpClientByUrl(url);
        url = addParams(url, params);
        HttpGet httpGet = new HttpGet(url);
        long start = System.currentTimeMillis();
        try {
            return doService(httpClient, httpGet, headers, cookies);
        } finally {
            LOGGER.info("get请求 url: {}, 花费时间: {} ms", url, System.currentTimeMillis() - start);
        }
    }

    public static HttpResponse doPost(String url, Map<String, String> params, Map<String, String> headers, Cookie... cookies) {
        if (StringUtils.isEmpty(url)) throw new IllegalArgumentException("url为空");
        CloseableHttpClient httpClient = createHttpClientByUrl(url);
        HttpPost httpPost = new HttpPost(url);
        addParams(httpPost, params);
        long start = System.currentTimeMillis();
        try {
            return doService(httpClient, httpPost, headers, cookies);
        } finally {
            LOGGER.info("post请求 url: {}, 花费时间: {} ms", url, System.currentTimeMillis() - start);
        }
    }

    private static void getCookies(HttpResponse result, CloseableHttpResponse httpResponse) {
        if (result == null) return;
        Header[] headers = httpResponse.getHeaders(HEADER_KEY);
        if (headers == null || headers.length <= 0) {
            return;
        }
        Map<String, Set<String>> cookies = new HashMap<>(headers.length);
        for (Header header : headers) {
            String[] cookieValues = header.getValue().split(";");
            if (cookieValues.length > 0) {
                for (String keyValue : cookieValues) {
                    if (StringUtil.isEmpty(keyValue)) continue;
                    String[] kv = keyValue.split("=");
                    String key = kv[0].trim();
                    Set<String> values = cookies.get(key);
                    if (kv.length == 2) {
                        if (values == null) values = new LinkedHashSet<>();
                        values.add(kv[1]);
                        cookies.put(key, values);
                    }
                }
            }
        }
        result.setCookies(cookies);
    }

    private static void addHeaders(HttpRequestBase request, Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                String value = headers.get(key);
                request.addHeader(key, value);
            }
        }
    }

    private static String addParams(String url, Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        builder.append(url);
        String paramStr = URLUtils.paramToQueryString(params);
        if (StringUtil.isNotEmpty(paramStr)) {
            if (url.contains("?")) {
                builder.append("&");
            } else {
                builder.append("?");
            }
            builder.append(paramStr);
        }
        return builder.toString();
    }

    private static void addParams(HttpPost httpPost, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            List<BasicNameValuePair> paramList = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                key = URLUtils.urlEncode(key, URLUtils.CHARSET);
                value = URLUtils.urlEncode(value, URLUtils.CHARSET);
                paramList.add(new BasicNameValuePair(key, value));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(paramList, Consts.UTF_8));
        }
    }

    private static void addCookies(AbstractHttpMessage httpMessage, Cookie[] cookies) {
        String cookieStr = assembleCookies(cookies);
        if (StringUtils.isNotEmpty(cookieStr)) {
            httpMessage.setHeader("Cookie", cookieStr);
        }
    }

    private static String assembleCookies(Cookie[] cookies) {

        if (cookies != null && cookies.length != 0) {
            StringBuilder builder = new StringBuilder();
            for (Cookie cookie : cookies) {
                builder.append(cookie.getName());
                builder.append("=");
                builder.append(cookie.getValue());
                builder.append(";");
            }
            return builder.toString();
        }

        return null;
    }

    public static CloseableHttpClient createHttpClientByUrl(String url) {

        CloseableHttpClient client = HttpClients.createDefault();
        if (StringUtils.equalsIgnoreCase(url.substring(0, 5), "https")) {
            try {
                HttpClientBuilder httpClientBuilder = HttpClients.custom();
                TrustManager[] tm = {new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};
                SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
                sslContext.init(null, tm, new java.security.SecureRandom());
                SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true);
                httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
                httpClientBuilder.setSslcontext(sslContext);
                client = httpClientBuilder.build();
            } catch (Exception e) {
                LOGGER.warn("create httpclient fail : {}", e.getMessage(), e);
                throw new IllegalStateException("create httpclient fail", e);
            }
        }

        return client;
    }

    public static class HttpResponse {

        private byte[] responseByte;
        private String responseBody;
        private Map<String, Set<String>> cookies;

        public byte[] getResponseByte() {
            return responseByte;
        }

        public void setResponseByte(byte[] responseByte) {
            this.responseByte = responseByte;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        public Map<String, Set<String>> getCookies() {
            return cookies;
        }

        public void setCookies(Map<String, Set<String>> cookies) {
            this.cookies = cookies;
        }
    }
}
