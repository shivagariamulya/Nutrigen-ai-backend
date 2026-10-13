package com.nutrigen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${frontend.url:${FRONTEND_URL:}}")
    private String frontendUrl;

    @Value("${cors.allowed-origins:}")
    private String corsAllowedOrigins;

    @Bean
    public SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        List<String> patterns = new ArrayList<>();
        addParsedOrigins(patterns, frontendUrl);
        addParsedOrigins(patterns, corsAllowedOrigins);

        if (patterns.isEmpty()) {
            patterns.add("*");
        } else {
            // Keep local dev origins available alongside production
            if (!patterns.contains("http://localhost:*")) patterns.add("http://localhost:*");
            if (!patterns.contains("http://127.0.0.1:*")) patterns.add("http://127.0.0.1:*");
            if (!patterns.contains("https://*.onrender.com")) patterns.add("https://*.onrender.com");
        }

        c.setAllowedOriginPatterns(patterns);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("*"));
        c.setAllowCredentials(false);
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }

    private void addParsedOrigins(List<String> list, String value) {
        if (value != null && !value.isBlank() && !value.equals("*")) {
            Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.replaceAll("/+$", ""))
                    .forEach(o -> {
                        if (!list.contains(o)) list.add(o);
                    });
        }
    }
}
