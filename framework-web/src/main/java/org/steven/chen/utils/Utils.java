package org.steven.chen.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class Utils {

    public static final String SYSTEM_ENCODING = System.getProperty("file.encoding");

    public static String getIPAndPort(HttpServletRequest request) {

        StringBuilder newIP = new StringBuilder(getIp(request));
        newIP.append(":");
        newIP.append(request.getRemotePort());

        return newIP.toString();
    }

    public static String getIp(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (StringUtils.isEmpty(ip) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)) {
            ip = "127.0.0.1";
        }

        if (ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }

        return ip;
    }

    public static String createCallBackUrl(String url, Logger LOGGER, HttpServletRequest request) {

        if (request == null) return null;

        StringBuilder sb = new StringBuilder(URLUtils.currentServerUrl(request));
        if (StringUtils.isNotBlank(url)) {
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
        }

        String callBackUrl = sb.toString();
        if (LOGGER != null) {
            LOGGER.info("createCallBackUrl callBackUrl:{}", callBackUrl);
        } else {
            System.out.println(sb.insert(0, "createCallBackUrl callBackUrl:").toString());
        }

        return callBackUrl;
    }

    /**
     * 读取IO流
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static String receiveRequestInput(HttpServletRequest request) throws IOException {

        int cnt;
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();

        InputStream inputStream = request.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        while ((cnt = bis.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, cnt));
        }

        bis.close();
        inputStream.close();

        return sb.toString();
    }

    public static void responseImage(HttpServletResponse response, File file) {

        String fileName = file.getName();
        String imageType = "image/" + fileName.substring(fileName.lastIndexOf(".") + 1);
        response.setContentType(imageType);

        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream ops = response.getOutputStream();

            int count;
            byte[] buffer = new byte[1024 * 1024];
            while ((count = fis.read(buffer)) != -1) {
                ops.write(buffer, 0, count);
                ops.flush();
            }

            ops.close();
            fis.close();
        } catch (FileNotFoundException e) {
            response.setStatus(404);
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    public static String file2String(File backList) throws IOException {

        int cnt;
        char[] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();

        FileInputStream fis = new FileInputStream(backList);
        InputStreamReader bis = new InputStreamReader(fis, SYSTEM_ENCODING);
        BufferedReader br = new BufferedReader(bis);
        while ((cnt = br.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, cnt));
        }

        br.close();
        bis.close();
        fis.close();

        return sb.toString();
    }
}
