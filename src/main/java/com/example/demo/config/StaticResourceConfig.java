package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static String configuredUploadDir = "uploads";

    public StaticResourceConfig(@Value("${app.upload.dir:uploads}") String uploadDir) {
        configuredUploadDir = uploadDir;
    }

    public static Path uploadRoot() {
        return Path.of(configuredUploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = uploadRoot();
        try {
            Files.createDirectories(root);
        } catch (Exception ignored) {
            // If creation fails here, controller will return a meaningful error on upload.
        }

        String uploadPath = root.toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
