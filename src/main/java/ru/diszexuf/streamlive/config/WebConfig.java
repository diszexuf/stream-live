package ru.diszexuf.streamlive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/avatars/**")
        .addResourceLocations("file:uploads/avatars/");
    registry.addResourceHandler("/uploads/thumbnails/**")
        .addResourceLocations("file:uploads/thumbnails/");
  }
}
