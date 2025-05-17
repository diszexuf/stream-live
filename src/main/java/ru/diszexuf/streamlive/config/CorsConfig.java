package ru.diszexuf.streamlive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешаем запросы с локального фронтенда
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // Разрешаем учетные данные
        config.setAllowCredentials(true);

        // Разрешаем все заголовки
        config.addAllowedHeader("*");

        // Разрешаем все методы
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // Экспозиция заголовков
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Content-Length", "Content-Disposition"));
        
        // Увеличиваем время кеширования предварительных запросов
        config.setMaxAge(3600L);

        // Регистрируем конфигурацию для всех путей, включая /api/**
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}