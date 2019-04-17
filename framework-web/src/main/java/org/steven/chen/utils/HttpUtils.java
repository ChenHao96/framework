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
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpUtils {

    public static Map<String, String> cookies = new HashMap<>(0);

    private static final String headerKey = "Set-Cookie";
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
        return doGet(url, params, null, cookies);
    }

    public static String doPost(String url, Map<String, String> params, Cookie... cookies) {
        return doPost(url, params, null, cookies);
    }

    public static String toGetUrl(String url, Map<String, String> params) {
        return addParams(url, params);
    }

    public static String doGet(String url, Map<String, String> params, Map<String, String> headers, Cookie... cookies) {

        long start = System.currentTimeMillis();
        if (StringUtils.isEmpty(url)) {
            LOGGER.warn("url为空");
            throw new IllegalArgumentException("url为空");
        } else {
            CloseableHttpClient httpClient = createHttpClientByUrl(url);
            CloseableHttpResponse httpResponse = null;
            HttpGet httpGet = null;

            String result;
            try {
                url = addParams(url, params);
                httpGet = new HttpGet(url);
                addCookies(httpGet, cookies);
                addHeaders(httpGet, headers);
                httpResponse = httpClient.execute(httpGet);
                result = toString(new InputStreamReader(httpResponse.getEntity().getContent(), Consts.UTF_8));
                getCookies(httpResponse);
            } catch (Exception e) {
                LOGGER.warn("Http doGet 异常 : {}", e.getMessage(), e);
                throw new IllegalStateException("Http doGet 异常", e);
            } finally {
                if (httpGet != null) {
                    httpGet.releaseConnection();
                }

                CommonsUtil.safeClose(httpResponse,httpClient);
            }

            LOGGER.info("get请求url: {}, 花费时间: {} ms", url, System.currentTimeMillis() - start);
            return result;
        }
    }

    public static String doPost(String url, Map<String, String> params, Map<String, String> headers, Cookie... cookies) {

        long start = System.currentTimeMillis();
        if (StringUtils.isEmpty(url)) {
            LOGGER.info("url为空");
            throw new IllegalArgumentException("url为空");
        } else {

            CloseableHttpClient httpClient = createHttpClientByUrl(url);
            CloseableHttpResponse httpResponse = null;
            HttpPost httpPost = null;

            String result;
            try {
                httpPost = new HttpPost(url);
                addParams(httpPost, params);
                addHeaders(httpPost, headers);
                addCookies(httpPost, cookies);
                httpResponse = httpClient.execute(httpPost);
                result = toString(new InputStreamReader(httpResponse.getEntity().getContent(), Consts.UTF_8));
                getCookies(httpResponse);
            } catch (Exception e) {
                LOGGER.warn("Http doPost 异常: {}", e.getMessage(), e);
                throw new IllegalStateException("Http doPost 异常", e);
            } finally {
                if (httpPost != null) {
                    httpPost.releaseConnection();
                }

                CommonsUtil.safeClose(httpResponse,httpClient);
            }

            LOGGER.info("post请求url: {}, 花费时间: {} ms", url, System.currentTimeMillis() - start);
            return result;
        }
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

    private static void getCookies(CloseableHttpResponse httpResponse) {

        Header[] headers = httpResponse.getHeaders(headerKey);
        if (headers == null || headers.length <= 0) {
            return;
        }

        cookies = new HashMap<>(headers.length);
        for (Header header : headers) {
            String[] cookieValues = header.getValue().split(";");
            if (cookieValues.length > 0) {
                for (String keyValue : cookieValues) {
                    String[] kv = keyValue.split("=");
                    if (kv.length < 2) {
                        cookies.put(kv[0].trim(), "");
                    } else {
                        cookies.put(kv[0].trim(), kv[1]);
                    }
                }
            }
        }
    }

    private static String addParams(String url, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(url);
            if (url.contains("?")) {
                builder.append("&");
            } else {
                builder.append("?");
            }

            for (String key : params.keySet()) {
                if (params.get(key) != null && !"".equals(params.get(key).trim())) {
                    String value = params.get(key).trim().replaceAll(" ", "%20").replaceAll("\t", "");
                    builder.append(key).append("=").append(value).append("&");
                }
            }

            url = builder.toString();
            if (url.endsWith("&")) {
                url = url.substring(0, url.lastIndexOf("&"));
            }
        }

        return url;
    }

    private static void addParams(HttpPost httpPost, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            ArrayList<BasicNameValuePair> paramList = new ArrayList<>();

            for (String key : params.keySet()) {
                if (params.get(key) != null && !"".equals(params.get(key).trim())) {
                    String value = params.get(key).trim().replaceAll(" ", "%20").replaceAll("\t", "");
                    paramList.add(new BasicNameValuePair(key, value));
                }
            }

            UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(paramList, Consts.UTF_8);
            httpPost.setEntity(entity1);
        }
    }

    private static void addCookies(HttpGet httpGet, Cookie[] cookies) {
        String cookieStr = assembleCookies(cookies);
        if (StringUtils.isNotEmpty(cookieStr)) {
            httpGet.setHeader("Cookie", cookieStr);
        }
    }

    private static void addCookies(HttpPost httpPost, Cookie[] cookies) {
        String cookieStr = assembleCookies(cookies);
        if (StringUtils.isNotEmpty(cookieStr)) {
            httpPost.setHeader("Cookie", cookieStr);
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
        CloseableHttpClient client;
        if (StringUtils.equalsIgnoreCase(url.substring(0, 5), "https")) {
            try {
                KeyStore e = KeyStore.getInstance(KeyStore.getDefaultType());
                HttpClientBuilder httpClientBuilder = HttpClients.custom();
                SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(e, (chain, authType) -> true).build();
                SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext, new X509HostnameVerifier() {
                    public void verify(String host, SSLSocket ssl) throws IOException {
                    }

                    public void verify(String host, X509Certificate cert) throws SSLException {
                    }

                    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                    }

                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });
                httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
                httpClientBuilder.setSSLContext(sslcontext);
                client = httpClientBuilder.build();
            } catch (Exception e) {
                LOGGER.warn("create httpclient fail : {}", e.getMessage(), e);
                throw new IllegalStateException("create httpclient fail", e);
            }
        } else {
            client = HttpClients.createDefault();
        }

        return client;
    }

    private static String toString(Readable from) throws IOException {

        StringBuilder sb = new StringBuilder();
        CharBuffer buf = CharBuffer.allocate(2048);

        while (from.read(buf) != -1) {
            buf.flip();
            sb.append(buf);
            buf.clear();
        }

        return sb.toString();
    }
}
