package com.villa.upload.local.util;

import com.villa.dto.FileMetadataDTO;
import com.villa.upload.local.config.LocalConfig;
import com.villa.upload.pinata.config.PinataConfig;
import com.villa.util.FileUtil;
import com.villa.util.SpringContextUtil;
import com.villa.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class LocalUploadUtil{
    private static LocalConfig localConfig = SpringContextUtil.getBean(LocalConfig.class);
    public static FileMetadataDTO upload(MultipartFile multipartFile) throws IOException {
        return upload(multipartFile, multipartFile.getOriginalFilename());
    }
    /**
     * 本地文件存储 仅返回一个相对的uri
     * @param multipartFile 上传时的源文件
     * @param fileName      文件名 会加载访问的网络路径上 如果文件名重复了 不会写操作 而是直接返回拼接的uri
     * @return 相对的网络路径
     * @throws IOException  文件找不到,或其他IO异常
     */
    public static FileMetadataDTO upload(MultipartFile multipartFile, String fileName) throws IOException {
        File file = new File(localConfig.getPath(), fileName);
        FileMetadataDTO metadataDTO = new FileMetadataDTO();
        metadataDTO.setUrl(localConfig.getNetPath()+fileName);
        metadataDTO.setName(multipartFile.getOriginalFilename());
        metadataDTO.setSize(multipartFile.getSize());
        metadataDTO.setMimeType(multipartFile.getContentType());
        metadataDTO.setType(FileUtil.getFileType(multipartFile.getContentType()));
        //如果存在 就直接返回路径
        if(file.exists()){
            return metadataDTO;
        }
        //文件夹不存在 创建文件夹
        File parentFile = file.getParentFile();
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        //创建文件
        file.createNewFile();
        //存储
        FileOutputStream out = new FileOutputStream(file);
        out.write(multipartFile.getBytes());
        out.close();
        return metadataDTO;
    }

    public static InputStream download(String s) {
        return null;
    }
}
