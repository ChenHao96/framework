package org.steven.chen.utils;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.steven.chen.model.AliyunProperties;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ChenHao on 2016/8/2.
 * 文件上传工具
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    public static final String SYSTEM_ENCODING = System.getProperty("file.encoding");
    public static final String FILE_PATH_STATIC = System.getProperty("user.home") + "/file_upload";

    private String filePath;
    private AliyunProperties properties;

    static {
        File file = new File(FILE_PATH_STATIC);
        if (file.setWritable(true)) {
            logger.info("filePath writable fail,create path:{}", FILE_PATH_STATIC);
        } else if (!file.exists()) {
            if (file.mkdirs()) {
                logger.info("filePath not exists,create path:{}", FILE_PATH_STATIC);
            }
        }
    }

    public FileUtil(AliyunProperties properties) {
        this.properties = properties;
    }

    private FileUtil(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 文件上传
     *
     * @param file spring框架的上传主件
     * @return 存放后的完整文件路径
     */
    public static String uploadFileLocal(MultipartFile file, String fileName) {
        return uploadFileLocal(file, FILE_PATH_STATIC, fileName);
    }

    /**
     * 文件上传
     *
     * @param file     上传的文件流
     * @param filePath 文件存放的位置
     * @param fileName 文件名称
     * @return 存放后的完整文件路径
     */
    public static String uploadFileLocal(MultipartFile file, String filePath, String fileName) {
        return new FileUtil(filePath).uploadFileOutPath(file, fileName);
    }

    /**
     * 文件上传
     *
     * @param file     spring框架的上传主件
     * @param fileName 文件名称
     * @return 存放后的完整文件路径
     */
    private String uploadFileOutPath(MultipartFile file, String fileName) {

        fileName = checkFileGetName(file, fileName);
        String dateDirectory = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String relativelyPath = File.separator + dateDirectory;

        File targetFile = new File(filePath + relativelyPath, fileName);
        logger.info("targetFile:{}", targetFile.toString());

        if (!targetFile.exists() && !targetFile.mkdirs()) {

            throw new RuntimeException("创建目录失败");
        } else {

            try {
                file.transferTo(targetFile);
            } catch (Exception e) {
                throw new RuntimeException("传输失败", e);
            }

            String absolutePath = targetFile.getAbsolutePath();
            logger.info("file absolutePath:{}", absolutePath);

            return (relativelyPath + "\\" + fileName).replaceAll("\\\\", "/");
        }
    }

    private String checkFileGetName(MultipartFile file, String fileName) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        if (StringUtils.isNotBlank(file.getOriginalFilename())) {
            fileName = file.getOriginalFilename();
        }
        logger.info("fileName:{}", fileName);

        String extName = fileName.substring(fileName.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        fileName = uuid + extName;
        logger.info("new fileName:{}", fileName);

        return fileName;
    }

    public String uploadWebFileOSS(MultipartFile file, String fileName) throws IOException {

        //获取文件新名称
        fileName = checkFileGetName(file, fileName);

        //获取文件流
        InputStream inputStream = file.getInputStream();
        return uploadFileOSS(fileName, inputStream);
    }

    public String uploadFileOSS(String fileName, InputStream inputStream) {

        //创建OSS连接实例
        ClientConfiguration config = new ClientConfiguration();
        config.setMaxErrorRetry(properties.getMaxErrorRetry());
        config.setSocketTimeout(properties.getSocketTimeout());
        config.setMaxConnections(properties.getMaxConnections());
        config.setConnectionTimeout(properties.getConnectionTimeout());
        CredentialsProvider credsProvider = new DefaultCredentialProvider(properties.getAccessKeyId(), properties.getAccessKeySecret());
        OSSClient ossClient = new OSSClient(properties.getEndpoint(), credsProvider, config);

        //写数据
        ossClient.putObject(properties.getBucketName(), fileName, inputStream);

        //创建访问路径
        Date expiration = new Date(System.currentTimeMillis() + 31536000000L);
        URL url = ossClient.generatePresignedUrl(properties.getBucketName(), fileName, expiration);

        //关闭OSS连接实例
        ossClient.shutdown();
        String resultUrl = url.toString();
        //将多余参数去除
        return resultUrl.substring(0, resultUrl.indexOf("?"));
    }

    public static String file2String(File file) throws IOException {

        int cnt;
        char[] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader bis = new InputStreamReader(fis, SYSTEM_ENCODING);
        BufferedReader br = new BufferedReader(bis);
        while ((cnt = br.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, cnt));
        }

        CommonsUtil.safeClose(br, bis, fis);
        return sb.toString();
    }
}
