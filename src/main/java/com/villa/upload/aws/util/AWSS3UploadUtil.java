package com.villa.upload.aws.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.villa.dto.FileMetadataDTO;
import com.villa.log.Log;
import com.villa.upload.aws.config.AWSS3Config;
import com.villa.util.FileUtil;
import com.villa.util.SpringContextUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** 上传文件工具类 */
public class AWSS3UploadUtil {
    private static final AmazonS3 amazonS3= SpringContextUtil.getBean(AmazonS3.class);
    private static AWSS3Config awsS3Config = SpringContextUtil.getBean(AWSS3Config.class);
    /** 从桶中删除文件 */
    public static void delete(String filename){
        DeleteObjectRequest request = new DeleteObjectRequest(awsS3Config.getBucket(), filename);
        amazonS3.deleteObject(request);
    }
    public static FileMetadataDTO upload(byte[] bs,String filename) throws IOException {
        return upload(new ByteArrayInputStream(bs),filename,false);
    }
    public static FileMetadataDTO upload(byte[] bs,String filename,boolean override) throws IOException {
        return upload(new ByteArrayInputStream(bs),filename,override);
    }
    /** 亚马逊S3上传文件 上传会自动生成文件名 根据文件md5命名 */
    public static FileMetadataDTO upload(MultipartFile file,boolean override)throws IOException{
        String suffix = FileUtil.getEndFix(file.getOriginalFilename());
        String filename = FileUtil.getFileMD5(file.getInputStream())+"."+suffix;
        return upload(file,filename,override);
    }
    /** 亚马逊S3上传文件 上传会自动生成文件名 根据文件md5命名 如果 */
    public static FileMetadataDTO upload(MultipartFile file)throws IOException{
        String suffix = FileUtil.getEndFix(file.getOriginalFilename());
        String filename = FileUtil.getFileMD5(file.getInputStream())+"."+suffix;
        return upload(file,filename,false);
    }
    public static FileMetadataDTO upload(MultipartFile file,String filename)throws IOException{
        return upload(file,filename,false);
    }
    /**
     * 亚马逊S3上传文件
     * @param file
     * @param filename  123.png这样的文件名
     * @param override 同名文件是否可覆盖
     */
    public static FileMetadataDTO upload(MultipartFile file,String filename,boolean override)throws IOException{
        return upload(file.getInputStream(),filename,override);
    }
    public static FileMetadataDTO upload(InputStream in, String filename)throws IOException{
        return upload(in,filename,false);
    }
    public static FileMetadataDTO upload(InputStream in, String filename,boolean override)throws IOException{
        long length = in.available();
        String suffix = FileUtil.getEndFix(filename);
        if(!override){
            FileMetadataDTO dto = exits(filename,null,null, in.available());
            if(dto!=null){
                return dto;
            }
        }
        //5m
        long partSize = 5 * 1024 * 1024L;
        int partCount = (int) (length / partSize);
        if (length % partSize != 0) {
            partCount++;
        }
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(awsS3Config.getBucket(), filename);
        InitiateMultipartUploadResult initResponse = amazonS3.initiateMultipartUpload(initRequest);
        List<PartETag> eTags = new ArrayList<>();

        BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
        bufferedInputStream.mark(Integer.MAX_VALUE); // 设置标记位置为流的开头
        for (int i = 0; i < partCount; i++) {
            long startPos = (long) i * partSize;
            //如果是最后一个分片 就用长度减去上一次的分片字节点
            long curPartSize = (i + 1L == partCount) ? (length - startPos) : partSize;
            // 重新定位输入流到正确的位置
            bufferedInputStream.reset();
            bufferedInputStream.skip(startPos);
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(awsS3Config.getBucket())
                    .withKey(filename)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(i + 1)
                    .withInputStream(bufferedInputStream)
                    .withPartSize(curPartSize);
            // 第二步，上传分段，并把当前段的 PartETag 放到列表中
            eTags.add(amazonS3.uploadPart(uploadRequest).getPartETag());
            try{
                Thread.sleep(25);
            }catch (Exception e){}
        }
        // 第三步，完成上传，合并分段
        CompleteMultipartUploadRequest req = new CompleteMultipartUploadRequest(awsS3Config.getBucket(), filename, initResponse.getUploadId(), eTags);
        // 返回文件元数据
        CompleteMultipartUploadResult result = amazonS3.completeMultipartUpload(req);
        //设置对象为公开访问权限
        amazonS3.setObjectAcl(awsS3Config.getBucket(), filename, CannedAccessControlList.PublicRead);
        String url = awsS3Config.getProtocol().toString()+"://"+result.getLocation();
        bufferedInputStream.close();
        return new FileMetadataDTO(filename,url,null,null,suffix,length);
    }
    public static FileMetadataDTO upload(File file, String filename, boolean override)throws IOException{
        return upload(Files.newInputStream(file.toPath()),filename,override);
    }
    /** 存在就返回 */
    private static FileMetadataDTO exits(String filename,String type,String mimeType,long size){
        //存在就返回已存在的元数据信息
        try{
            if(amazonS3.doesObjectExist(awsS3Config.getBucket(), filename)){
                return new FileMetadataDTO(filename,amazonS3.getUrl(awsS3Config.getBucket(),filename).toString(),
                        type,mimeType,FileUtil.getEndFix(filename),size);
            }
        }catch (Exception e){
            Log.err("【S3上传】判断文件是否存在失败,文件将会覆盖");
        }
        return null;
    }
}
