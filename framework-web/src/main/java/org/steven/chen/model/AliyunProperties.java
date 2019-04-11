package org.steven.chen.model;

public class AliyunProperties {

    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AliyunProperties{");
        sb.append("endpoint='").append(endpoint).append('\'');
        sb.append(", bucketName='").append(bucketName).append('\'');
        sb.append(", accessKeyId='").append(accessKeyId).append('\'');
        sb.append(", accessKeySecret='").append(accessKeySecret).append('\'');
        sb.append(", securityToken='").append(securityToken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
