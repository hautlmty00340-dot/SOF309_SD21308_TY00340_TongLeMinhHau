package com.aubadminton.config;

import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AvatarResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String avatars = Paths.get("uploads", "avatars").toAbsolutePath().toUri().toString();
        if (!avatars.endsWith("/")) avatars += "/";
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatars);
    }
}
