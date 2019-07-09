package com.github.chenhao96.utils;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.github.chenhao96.model.AliyunProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileUploadUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);
    private static final String FILE_PATH_STATIC = System.getProperty("user.home") + "/file_upload";

    private boolean local;
    private OSSClient ossClient;
    private final String filePath;
    private AliyunProperties properties;

    public FileUploadUtil() {
        this.filePath = FILE_PATH_STATIC;
    }

    public FileUploadUtil(String filePath) {
        this.filePath = filePath;
    }

    public FileUploadUtil(AliyunProperties properties) {
        this.properties = properties;
        this.filePath = FILE_PATH_STATIC;
    }

    public String getFilePath() {
        return filePath;
    }

    public void localInit() {
        this.local = true;
        File file = new File(filePath);
        if (file.setWritable(true)) {
            logger.info("filePath writable fail,create path:{}", filePath);
        } else {
            if (!file.exists() && file.mkdirs()) {
                logger.info("filePath not exists,create path:{}", filePath);
            }
        }
    }

    public void ossInit() {
        if (this.properties != null) {
            this.local = false;

            ClientConfiguration config = new ClientConfiguration();
            config.setMaxErrorRetry(properties.getMaxErrorRetry());
            config.setSocketTimeout(properties.getSocketTimeout());
            config.setMaxConnections(properties.getMaxConnections());
            config.setConnectionTimeout(properties.getConnectionTimeout());
            CredentialsProvider credsProvider = new DefaultCredentialProvider(
                    properties.getAccessKeyId(), properties.getAccessKeySecret());
            this.ossClient = new OSSClient(properties.getEndpoint(), credsProvider, config);
        }
    }

    public void ossDestroy() {
        if (this.ossClient != null) {
            this.ossClient.shutdown();
        }
    }

    public String getBucketName() {
        if (this.properties != null) {
            return this.properties.getBucketName();
        }
        return null;
    }

    public boolean isLocal() {
        return local;
    }

    public FileInfo uploadFileOutPath(MultipartFile file, String fileName) throws IOException {
        fileName = this.checkFileGetName(file, fileName);
        File targetFile = new File(this.filePath, fileName);
        logger.info("targetFile:{}", targetFile.toString());
        if (!targetFile.exists() && !targetFile.mkdirs()) {
            throw new RuntimeException("创建目录失败");
        } else {
            try {
                file.transferTo(targetFile);
            } catch (Exception var7) {
                throw new RuntimeException("传输失败", var7);
            }
            String absolutePath = targetFile.getAbsolutePath();
            logger.info("file absolutePath:{}", absolutePath);
            FileInfo info = new FileInfo();
            info.fileMd5 = DigestUtils.md5Hex(file.getInputStream());
            info.filePath = (fileName).replaceAll("\\\\", "/");
            info.responseUrl = info.filePath;
            return info;
        }
    }

    private String checkFileGetName(MultipartFile file, String fileName) {
        if (file != null && !file.isEmpty()) {
            if (StringUtils.isNotBlank(file.getOriginalFilename())) {
                fileName = file.getOriginalFilename();
            }
            logger.info("fileName:{}", fileName);
            String extName = fileName.substring(fileName.lastIndexOf("."));
            String uuid = UUID.randomUUID().toString().replace("-", "");
            fileName = uuid + extName;
            logger.info("new fileName:{}", fileName);
            String dateDirectory = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
            String relativelyPath = File.separator + dateDirectory + File.separator;
            return relativelyPath + fileName;
        } else {
            throw new IllegalArgumentException("上传文件不能为空");
        }
    }

    public FileInfo uploadWebFileOSS(MultipartFile file, String fileName) throws IOException {
        fileName = this.checkFileGetName(file, fileName);
        InputStream inputStream = file.getInputStream();
        return this.uploadFileOSS(fileName, inputStream);
    }

    public FileInfo uploadFileOSS(String fileName, InputStream inputStream) throws IOException {
        this.ossClient.putObject(this.properties.getBucketName(), fileName, inputStream);
        Date expiration = new Date(System.currentTimeMillis() + 31536000000L);
        URL url = this.ossClient.generatePresignedUrl(this.properties.getBucketName(), fileName, expiration);
        String resultUrl = url.toString();
        FileInfo info = new FileInfo();
        info.filePath = fileName;
        info.fileMd5 = DigestUtils.md5Hex(inputStream);
        info.responseUrl = resultUrl.substring(0, resultUrl.indexOf("?"));
        return info;
    }

    public static String file2String(File file) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return IOUtils.readStream2String(fileInputStream, CommonsUtil.SYSTEM_ENCODING);
        } finally {
            CommonsUtil.safeClose(fileInputStream);
        }
    }

    public class FileInfo {
        private String filePath;
        private String fileMd5;
        private String responseUrl;

        public String getResponseUrl() {
            return responseUrl;
        }

        public void setResponseUrl(String responseUrl) {
            this.responseUrl = responseUrl;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getFileMd5() {
            return fileMd5;
        }
    }
}
