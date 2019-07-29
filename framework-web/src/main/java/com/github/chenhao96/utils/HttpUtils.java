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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class HttpUtils {

    private static final String HEADER_KEY = "Set-Cookie";
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private String charSet;
    private String responseBody;
    private final boolean encode;
    private Map<String, Set<String>> cookies;

    public HttpUtils(boolean encode) {
        this.encode = encode;
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Map<String, Set<String>> getCookies() {
        return cookies;
    }

    public String doGet(String url) {
        return doGet(url, null);
    }

    public String doPost(String url) {
        return doPost(url, null);
    }

    public String doGet(String url, Map<String, String> params) {
        return doGet(url, params, null);
    }

    public String doPost(String url, Map<String, String> params) {
        return doPost(url, params, null);
    }

    public String doGet(String url, Map<String, String> params, Map<String, String> headers) {
        return doGet(url, params, headers, new Cookie[0]).responseBody;
    }

    public String doPost(String url, Map<String, String> params, Map<String, String> headers) {
        return doPost(url, params, headers, new Cookie[0]).responseBody;
    }

    public String toGetUrl(String url, Map<String, String> params) {
        return addParams(url, params);
    }

    public HttpUtils doService(CloseableHttpClient httpClient, HttpRequestBase requestBase, Map<String, String> headers, Cookie... cookies) {
        if (requestBase == null) return this;
        this.cookies = null;
        this.responseBody = null;
        CloseableHttpResponse httpResponse = null;
        long start = System.currentTimeMillis();
        try {
            addCookies(requestBase, cookies);
            addHeaders(requestBase, headers);
            httpResponse = httpClient.execute(requestBase);
            String charSet = getResponseCharSet(httpResponse);
            HttpEntity entity = httpResponse.getEntity();
            String responseBody = EntityUtils.toString(entity, charSet);
            EntityUtils.consume(entity);
            this.responseBody = responseBody;
            putCookies(httpResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Http doService 异常", e);
        } finally {
            LOGGER.info("请求花费时间: {} ms", System.currentTimeMillis() - start);
            requestBase.releaseConnection();
            CommonsUtil.safeClose(httpResponse, httpClient);
        }
        return this;
    }

    public HttpUtils doGet(String url, Map<String, String> params, Map<String, String> headers, Cookie... cookies) {
        if (StringUtil.isEmpty(url)) throw new IllegalArgumentException("url为空");
        CloseableHttpClient httpClient = createHttpClientByUrl(url);
        url = addParams(url, params);
        HttpGet httpGet = new HttpGet(url);
        try {
            return doService(httpClient, httpGet, headers, cookies);
        } finally {
            LOGGER.info("get请求 url: {}", url);
        }
    }

    public HttpUtils doPost(String url, Map<String, String> params, Map<String, String> headers, Cookie... cookies) {
        if (StringUtil.isEmpty(url)) throw new IllegalArgumentException("url为空");
        CloseableHttpClient httpClient = createHttpClientByUrl(url);
        HttpPost httpPost = new HttpPost(url);
        addParams(httpPost, params);
        try {
            return doService(httpClient, httpPost, headers, cookies);
        } finally {
            LOGGER.info("post请求 url: {}", url);
        }
    }

    private void putCookies(CloseableHttpResponse httpResponse) {
        Header[] headers = httpResponse.getHeaders(HEADER_KEY);
        if (headers == null || headers.length <= 0) return;
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
        this.cookies = cookies;
    }

    private String addParams(String url, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            StringBuilder builder = new StringBuilder(url.length());
            Set<Map.Entry<String, String>> entries = params.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (StringUtil.isNotBlank(value)) {
                    builder.append("&").append(key).append("=");
                    value = value.trim();
                    try {
                        if (encode) value = URLEncoder.encode(value, charSet);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    builder.append(value);
                }
            }
            if (url.indexOf("?") > 0 && url.indexOf("?") < url.length() - 1) {
                url = builder.insert(0, url).toString();
            } else {
                url = builder.replace(0, 1, "?").insert(0, url).toString();
            }
        }
        return url;
    }

    private void addParams(HttpPost httpPost, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            List<BasicNameValuePair> paramList = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (StringUtil.isNotBlank(value)) {
                    paramList.add(new BasicNameValuePair(key, value.trim()));
                }
            }
            try {
                if (encode) httpPost.setEntity(new UrlEncodedFormEntity(paramList, charSet));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void addHeaders(HttpRequestBase request, Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            Set<Map.Entry<String, String>> entries = headers.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (StringUtil.isEmpty(entry.getValue())) continue;
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private static String getResponseCharSet(CloseableHttpResponse httpResponse) {
        String charSet = CommonsUtil.SYSTEM_ENCODING;
        Header contentType = httpResponse.getFirstHeader(CONTENT_TYPE_KEY);
        if (contentType != null) {
            String contextTypeValue = contentType.getValue();
            int index = contextTypeValue.toUpperCase().indexOf("CHARSET");
            if (index < 1) return charSet;
            charSet = contextTypeValue.substring(index);
            charSet = charSet.substring(charSet.indexOf("=") + 1).replace(";", "");
        }
        return charSet;
    }

    private static void addCookies(AbstractHttpMessage httpMessage, Cookie[] cookies) {
        String cookieStr = assembleCookies(cookies);
        if (StringUtil.isNotEmpty(cookieStr)) {
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
        if (StringUtil.equalsIgnoreCase(url.substring(0, 5), "https")) {
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
}
