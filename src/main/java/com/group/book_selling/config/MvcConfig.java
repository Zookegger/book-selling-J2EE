package com.group.book_selling.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadRootDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(uploadRootDir).toAbsolutePath().normalize();
        String uploadPath = uploadDir.toUri().toString();

        // Mọi request bắt đầu bằng /uploads/ sẽ được trỏ tới thư mục uploads trên ổ cứng
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}