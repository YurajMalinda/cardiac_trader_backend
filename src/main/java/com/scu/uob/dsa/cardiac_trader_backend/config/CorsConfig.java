package com.scu.uob.dsa.cardiac_trader_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for React Frontend
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed.origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${cors.allowed.methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${cors.allowed.headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow.credentials:true}")
    private boolean allowCredentials;

    @Override
    @SuppressWarnings("null")
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        
        // Handle headers - if "*" is specified and credentials are allowed, use default headers
        List<String> headersList;
        if ("*".equals(allowedHeaders.trim()) && allowCredentials) {
            // When credentials are allowed, we can't use "*" for headers
            // Use common CORS headers instead
            headersList = Arrays.asList("Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin");
        } else {
            headersList = Arrays.asList(allowedHeaders.split(","));
        }
        
        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(new String[0]))  // NOSONAR - array conversion needed
                .allowedMethods(allowedMethods.split(","))  // NOSONAR - array conversion needed
                .allowedHeaders(headersList.toArray(new String[0]))  // NOSONAR - array conversion needed
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse origins
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // Parse methods
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        
        // Handle headers - if "*" is specified and credentials are allowed, use default headers
        if ("*".equals(allowedHeaders.trim()) && allowCredentials) {
            // When credentials are allowed, we can't use "*" for headers
            // Use common CORS headers instead
            configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}

