package com.villa.upload.aws.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.villa.upload.aws.config.AWSS3Config;
import com.villa.dto.FileMetadataDTO;
import com.villa.util.FileUtil;
import com.villa.util.SpringContextUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 上传文件工具类
 */
public class AWSS3UploadUtil {

    private static final AmazonS3 amazonS3= SpringContextUtil.getBean(AmazonS3.class);
    private static AWSS3Config awsS3Config = SpringContextUtil.getBean(AWSS3Config.class);
    /**
     * 亚马逊S3上传文件 上传会自动生成文件名 根据文件md5命名 如果
     * @param file
     */
    public static FileMetadataDTO upload(MultipartFile file,boolean override)throws IOException{
        String suffix = FileUtil.getEndFix(file.getOriginalFilename());
        String fileName = FileUtil.getFileMD5(file.getInputStream())+"."+suffix;
        return upload(file,fileName,override);
    }
    /**
     * 亚马逊S3上传文件 上传会自动生成文件名 根据文件md5命名 如果
     * @param file
     */
    public static FileMetadataDTO upload(MultipartFile file)throws IOException{
        String suffix = FileUtil.getEndFix(file.getOriginalFilename());
        String fileName = FileUtil.getFileMD5(file.getInputStream())+"."+suffix;
        return upload(file,fileName,false);
    }
    public static FileMetadataDTO upload(MultipartFile file,String fileName)throws IOException{
        return upload(file,fileName,false);
    }
    /**
     * 亚马逊S3上传文件
     * @param file
     * @param fileName  123.png这样的文件名
     * @param override 同名文件是否可覆盖
     */
    public static FileMetadataDTO upload(MultipartFile file,String fileName,boolean override)throws IOException{
        String suffix = FileUtil.getEndFix(fileName);
        String type = FileUtil.getFileType(file.getContentType());
        if(!override){
            FileMetadataDTO dto = exits(fileName,type,file.getContentType(),file.getSize());
            if(dto!=null){
                return dto;
            }
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        PutObjectRequest request = new PutObjectRequest(awsS3Config.getBucket(), fileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(request);
        file.getInputStream().close();
        return new FileMetadataDTO(fileName,amazonS3.getUrl(awsS3Config.getBucket(),fileName).toString(),
                type,file.getContentType(),suffix,file.getSize());
    }
    public static FileMetadataDTO upload(InputStream in, String fileName)throws IOException{
        return upload(in,fileName,false);
    }
    public static FileMetadataDTO upload(InputStream in, String fileName,boolean override)throws IOException{
        String suffix = FileUtil.getEndFix(fileName);
        if(!override){
            FileMetadataDTO dto = exits(fileName,null,null, in.available());
            if(dto!=null){
                return dto;
            }
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(in.available());
        PutObjectRequest request = new PutObjectRequest(awsS3Config.getBucket(), fileName, in, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(request);
        in.close();
        return new FileMetadataDTO(fileName,amazonS3.getUrl(awsS3Config.getBucket(),fileName).toString(),
                null,null,suffix,(long)in.available());
    }
    public static FileMetadataDTO upload(File file, String fileName, boolean override)throws IOException{
        String suffix = FileUtil.getEndFix(fileName);
        if(!override){
            FileMetadataDTO dto = exits(fileName,null,null, file.length());
            if(dto!=null){
                return dto;
            }
        }
        PutObjectRequest request = new PutObjectRequest(awsS3Config.getBucket(), fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(request);
        return new FileMetadataDTO(fileName,amazonS3.getUrl(awsS3Config.getBucket(),fileName).toString(),
                null,null,suffix, file.length());
    }
    /**
     * 存在就返回
     */
    private static FileMetadataDTO exits(String fileName,String type,String mimeType,long size){
        //存在就返回已存在的元数据信息
        if(amazonS3.doesObjectExist(awsS3Config.getBucket(), fileName)){
            return new FileMetadataDTO(fileName,amazonS3.getUrl(awsS3Config.getBucket(),fileName).toString(),
                    type,mimeType,FileUtil.getEndFix(fileName),size);
        }
        //没有Bucket就创建Bucket
//        if(!amazonS3.doesBucketExistV2(awsS3Config.getBucket())){
//            amazonS3.createBucket(awsS3Config.getBucket());
//        }
        return null;
    }
}
