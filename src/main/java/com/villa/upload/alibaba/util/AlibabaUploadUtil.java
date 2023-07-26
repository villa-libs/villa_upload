package com.villa.upload.alibaba.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.villa.upload.alibaba.config.AlibabaConfig;
import com.villa.util.SpringContextUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class AlibabaUploadUtil {
    private static AlibabaConfig alibaba = SpringContextUtil.getBean(AlibabaConfig.class);
    /**
     * @param fileName 未加文件后缀名
     */
    public static String upload(MultipartFile file, String fileName) throws IOException{
        return hostWindsUpload(file.getInputStream(),fileName);
    }
    public static String upload(InputStream in, String fileName) throws IOException{
        return hostWindsUpload(in,fileName);
    }
    public static String upload(File in, String fileName) throws IOException{
        return hostWindsUpload(new FileInputStream(in),fileName);
    }
    //下载返回一个流
    public static InputStream downLoad(String fileName){
        OSS ossClient = new OSSClientBuilder().build(alibaba.getEndpoint(), alibaba.getId(), alibaba.getSecret());
        OSSObject ossObject = ossClient.getObject(alibaba.getBucket(), fileName);
        return ossObject.getObjectContent();
    }

    private static AmazonS3Client getClient(){
        ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(Protocol.HTTPS);
        config.setSignerOverride("S3SignerType");
        AmazonS3Client client = new AmazonS3Client(
                new BasicAWSCredentials(alibaba.getId(), alibaba.getSecret()),config);
        client.setEndpoint(alibaba.getEndpoint());
        return client;
    }
    private static String hostWindsUpload(InputStream in, String fileName) throws IOException {
        ObjectMetadata meta = new ObjectMetadata();
        // 设置对象大小
        meta.setContentLength(in.available());
        AmazonS3Client client = getClient();
        client.putObject(alibaba.getBucket(),fileName,in,meta);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(alibaba.getBucket(), fileName);
        // 设置过期时间，当到达该时间点时， URL 就会过期，其他人不再能访问该对象。
        request.setExpiration(new Date(System.currentTimeMillis() + (1000L*60L*60L*24L*365L*100L)));
        return client.generatePresignedUrl(request).toString();
    }
}
