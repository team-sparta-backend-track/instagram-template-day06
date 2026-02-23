package com.example.instagramclone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.location}")
    private String fileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/img/**"로 들어오는 모든 요청을 물리적 폴더(fileDir)로 연결합니다.
        // 이때 "file:" 접두어를 붙여야 로컬 파일 시스템을 가리킵니다.
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:" + fileDir);
    }
}
