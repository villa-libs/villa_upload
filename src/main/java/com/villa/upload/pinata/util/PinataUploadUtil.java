package com.villa.upload.pinata.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.villa.dto.FileMetadataDTO;
import com.villa.upload.pinata.config.InputStreamRequestBody;
import com.villa.upload.pinata.config.PinataConfig;
import com.villa.util.FileUtil;
import com.villa.util.SpringContextUtil;
import com.villa.util.Util;
import okhttp3.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class PinataUploadUtil {
    private static PinataConfig pinata = SpringContextUtil.getBean(PinataConfig.class);

    /**
     *
     * @param pinataBody
     * @return
     */
    public static FileMetadataDTO pinJsonToIpfs(String pinataBody) throws IOException {
        if(Util.isNullOrEmpty(pinataBody)){
            return null;
        }
        return pinJsonToIpfs(JSON.parseObject(pinataBody),null);
    }
    /**
     * 上传jSON到IPFS
     * @param pinataBody   json字符串
     * @param options      额外的附加参数
     * @return 返回IPFS的hash
     * */
    public static FileMetadataDTO pinJsonToIpfs(JSONObject pinataBody, JSONObject options) throws IOException {
        JSONObject bodyContent = pinataBody;
        if (options != null) {
            bodyContent = new JSONObject();
            bodyContent.put("pinataContent", pinataBody);

            if (options.containsKey("pinataOptions")) {
                bodyContent.put("pinataOptions", options.getJSONObject("pinataOptions"));
            }
            if (options.containsKey("pinataMetadata")) {
                bodyContent.put("pinataMetadata", options.getJSONObject("pinataMetadata"));
            }
        }
        String endpoint = pinata.getHost() + "/pinning/pinJSONToIPFS";
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(bodyContent.toString(), mediaType);
        FileMetadataDTO metadataDTO = postOrPutRequest("POST", endpoint, body, pinata.getKey(), pinata.getSecret());
        metadataDTO.setType("txt");
        metadataDTO.setMimeType("text/plain");
        metadataDTO.setName("unknown");
        metadataDTO.setSuffix("txt");
        return metadataDTO;
    }
    /**
     * 上传j文件到IPFS
     * @param file      上传的文件
     * @return 返回文件元数据
     * */
    public static FileMetadataDTO pinFileToIpfs(MultipartFile file) throws IOException {
        return pinFileToIpfs(file, (JSONObject) null);
    }
    /**
     * 上传j文件到IPFS
     * @param file      上传的文件
     * @param options   额外参数的json字符串
     * @return 返回文件元数据
     * */
    public static FileMetadataDTO pinFileToIpfs(MultipartFile file,String options) throws IOException {
        if(Util.isNotNullOrEmpty(options)){
            return pinFileToIpfs(file,JSON.parseObject(options));
        }
        return pinFileToIpfs(file, JSON.parseObject(options));
    }
    /**
     * 上传j文件到IPFS
     * @param file      上传的文件
     * @param options   额外参数的json字符串
     * @return 返回文件元数据
     * */
    public static FileMetadataDTO pinFileToIpfs(File file, String options) throws IOException {
        return pinFileToIpfs(new FileInputStream(file),options);
    }
    /**
     * 上传j文件到IPFS
     * @param in        输入流
     * @param fileName  文件名称,带后缀的
     * @return 返回文件元数据
     * */
    public static FileMetadataDTO pinFileToIpfs(InputStream in,String fileName) throws IOException {
        return pinFileToIpfs(in,null,fileName,FileUtil.getEndFix(fileName),null);
    }
    /**
     * 上传j文件到IPFS
     * @param file      上传的文件
     * @param options   额外参数
     * @return 返回文件元数据
     * */
    public static FileMetadataDTO pinFileToIpfs(MultipartFile file,JSONObject options) throws IOException {
        String suffix = FileUtil.getEndFix(file.getOriginalFilename());
        String fileName = FileUtil.getFileMD5(file.getInputStream())+"."+suffix;
        return pinFileToIpfs(file.getInputStream(),options,fileName,suffix,file.getContentType());
    }
    public static FileMetadataDTO pinFileToIpfs(InputStream inputStream, JSONObject options,String fileName,String suffix,String mimeType) throws IOException {
        String endpoint = pinata.getHost() + "/pinning/pinFileToIPFS";
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        InputStreamRequestBody.create(inputStream, MediaType.parse("application/octet-stream")));
        if (options != null) {
            addOptionsToMultiPartBody(bodyBuilder, options);
        }
        RequestBody body = bodyBuilder.build();
        FileMetadataDTO metadataDTO = postOrPutRequest("POST", endpoint, body, pinata.getKey(), pinata.getSecret());
        //有类型才封装类型 否则不封装
        if(Util.isNotNullOrEmpty(mimeType)){
            metadataDTO.setType(FileUtil.getFileType(mimeType));
            metadataDTO.setMimeType(mimeType);
        }
        metadataDTO.setName(fileName);
        metadataDTO.setSuffix(suffix);
        return metadataDTO;
    }
    /**
     * 添加额外参数
     * @param bodyBuilder
     * @param options
     */
    private static MultipartBody.Builder addOptionsToMultiPartBody(MultipartBody.Builder bodyBuilder,
                                                            JSONObject options){
        if (options.containsKey("pinataOptions")) {
            bodyBuilder.addFormDataPart("pinataOptions",
                    options.getJSONObject("pinataOptions").toString());
        }
        if (options.containsKey("pinataMetadata")) {
            bodyBuilder.addFormDataPart("pinataMetadata",
                    options.getJSONObject("pinataMetadata").toString());
        }
        return bodyBuilder;
    }
    private static FileMetadataDTO postOrPutRequest(String method, String endpoint, RequestBody requestBody,
                                                  String pinataApiKey, String pinataSecretApiKey) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60*60, TimeUnit.SECONDS)
                .writeTimeout(60*60,TimeUnit.SECONDS)
                .callTimeout(60*60,TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(endpoint)
                .method(method, requestBody)
                .addHeader("pinata_api_key", pinataApiKey)
                .addHeader("pinata_secret_api_key", pinataSecretApiKey)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        if (response.code() != 200) {
            throw new RuntimeException(
                    "unknown server response while adding to pin queue: " + responseBody);
        }
        FileMetadataDTO metadataDTO = new FileMetadataDTO();
        JSONObject bodyJSON = JSONObject.parseObject(responseBody);
        metadataDTO.setUrl(bodyJSON.getString("IpfsHash"));
        metadataDTO.setSize(bodyJSON.getLongValue("PinSize"));
        return metadataDTO;
    }
}
