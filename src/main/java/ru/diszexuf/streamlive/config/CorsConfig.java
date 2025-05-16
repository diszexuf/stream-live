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

        // Разрешить конкретный origin (без "*")
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // Разрешить отправку credentials (cookies, Authorization header)
        config.setAllowCredentials(true);

        // Разрешить конкретные заголовки
        config.setAllowedHeaders(Arrays.asList(
            "Origin",
            "Content-Type",
            "Accept",
            "Authorization"
        ));

        // Разрешить методы
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Установить exposed headers
        config.setExposedHeaders(List.of("Authorization"));

        // Max age для preflight
        config.setMaxAge(3600L);

        // Применить ко всем путям
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}