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

    public String getBucket() {
        return bucket;
    }

    public Protocol getProtocol() {
        return endpoint !=null && endpoint.startsWith("https") ? Protocol.HTTPS : Protocol.HTTP;
    }

    @Bean
    public AmazonS3 amazonS3() {
        //设置S3缓冲区大小
        System.setProperty("com.amazonaws.sdk.s3.defaultStreamBufferSize", "20971520");
        AWSCredentials credentials = new BasicAWSCredentials(id, secret);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(getProtocol());
        config.disableSocketProxy();
        config.setSocketTimeout(60*60*1000);
        config.setConnectionTimeout(60*60*1000);
        config.setRequestTimeout(60*60*1000);
        config.withClientExecutionTimeout(60*60*1000);

        return AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(credentialsProvider)
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true)
                .build();
    }
}
