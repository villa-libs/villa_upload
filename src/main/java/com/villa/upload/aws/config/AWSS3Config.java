package com.villa.upload.aws.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AWSS3Config {
    /** 没有默认值 代表可以不启用AWS S3 */
    @Value("${aws.id:}")
    private String id;
    @Value("${aws.secret:}")
    private String secret;
    @Value("${aws.region:}")
    private String region;
    @Value("${aws.bucket:}")
    private String bucket;
    @Value("${aws.endpoint:https://sevensea.s3.ap-southeast-1.amazonaws.com}")
    private String endpoint;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Bean
    public AmazonS3 amazonS3() {
        //设置S3缓冲区大小
        System.setProperty("com.amazonaws.sdk.s3.defaultStreamBufferSize", "20971520");
        AWSCredentials credentials = new BasicAWSCredentials(id, secret);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        ClientConfiguration config = new ClientConfiguration();
//        config.setSignerOverride("S3SignerType");//预签名
        config.setProtocol(Protocol.HTTP);
//        config.withUseExpectContinue(false);
        config.disableSocketProxy();

        AmazonS3 client = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(credentialsProvider)
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true)
//                .withForceGlobalBucketAccessEnabled(true)
                .build();

        return client;
    }
}
