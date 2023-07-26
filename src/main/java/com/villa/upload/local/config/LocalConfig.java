package com.villa.upload.local.config;

import com.villa.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
@Configuration
public class LocalConfig implements WebMvcConfigurer {
    /**
     * 此路径为磁盘路径 将此路径映射到 /upload/ 的网络路径
     */
    @Value("${local.path:auto}")
    private String path;
    /**
     * 网络路径 默认/upload/
     */
    @Value("${local.netPath:/upload/}")
    private String netPath;

    /**
     * 解决磁盘路径中带有中文的问题
     * @param configurer
     */
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper=new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        urlPathHelper.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configurer.setUrlPathHelper(urlPathHelper);
    }
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //如果用户使用自动模式 则在项目文件夹生成一个upload文件夹
        if("auto".equalsIgnoreCase(path)){
            path = System.getProperty("user.dir")+netPath;
        }
        //开启了本地存储才添加映射
        if(Util.isNotNullOrEmpty(path)){
            registry.addResourceHandler(netPath+"**").addResourceLocations("file:"+path);
        }
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNetPath() {
        return netPath;
    }

    public void setNetPath(String netPath) {
        this.netPath = netPath;
    }
}
