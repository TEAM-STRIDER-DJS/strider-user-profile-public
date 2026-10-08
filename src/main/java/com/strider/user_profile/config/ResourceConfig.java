package com.strider.user_profile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 절대 경로 변환
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();

        registry.addResourceHandler("/static/uploads/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/")
                .setCachePeriod(0); // 캐시 무효화
    }
}