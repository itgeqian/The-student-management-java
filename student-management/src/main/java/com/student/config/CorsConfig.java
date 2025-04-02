package com.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 设置允许的源
        config.setAllowedOrigins(Arrays.asList("http://localhost:8080","http://localhost:3000"));
        
        // 设置允许的 HTTP 方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 设置允许的头
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // 设置暴露的头
        config.setExposedHeaders(Collections.singletonList("Authorization"));
        
        // 设置是否允许凭证
        config.setAllowCredentials(true);
        
        // 设置预检请求的缓存时间
        config.setMaxAge(1800L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
