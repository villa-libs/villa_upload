package com.villa.upload.alibaba.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class AlibabaConfig {
    @Value("${alibaba.id:}")
    private String id;
    @Value("${alibaba.secret:}")
    private String secret;
    @Value("${alibaba.endpoint:}")
    private  String endpoint;
    @Value("${alibaba.cdnHost:}")
    private  String  cdnHost;
    @Value("${alibaba.bucket:}")
    private  String  bucket;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getCdnHost() {
        return cdnHost;
    }

    public void setCdnHost(String cdnHost) {
        this.cdnHost = cdnHost;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
