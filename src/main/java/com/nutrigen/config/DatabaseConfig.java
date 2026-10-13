package com.nutrigen.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();

        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DATABASE_URL");
        }

        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        if (dbUrl != null && !dbUrl.isBlank()) {
            dbUrl = dbUrl.trim();

            // Auto-convert standard Render PostgreSQL URL format (postgres:// or postgresql://)
            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                try {
                    URI uri = new URI(dbUrl.replace("postgresql://", "http://").replace("postgres://", "http://"));
                    String host = uri.getHost();
                    int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                    String path = uri.getPath();
                    String userInfo = uri.getUserInfo();

                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        if (dbUser == null || dbUser.isBlank()) dbUser = parts[0];
                        if (dbPass == null || dbPass.isBlank()) dbPass = parts[1];
                    }

                    dbUrl = "jdbc:postgresql://" + host + ":" + port + path;
                    properties.setDriverClassName("org.postgresql.Driver");
                } catch (Exception ignored) {}
            } else if (dbUrl.startsWith("mysql://")) {
                try {
                    URI uri = new URI(dbUrl.replace("mysql://", "http://"));
                    String host = uri.getHost();
                    int port = uri.getPort() != -1 ? uri.getPort() : 3306;
                    String path = uri.getPath();
                    String userInfo = uri.getUserInfo();

                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        if (dbUser == null || dbUser.isBlank()) dbUser = parts[0];
                        if (dbPass == null || dbPass.isBlank()) dbPass = parts[1];
                    }

                    dbUrl = "jdbc:mysql://" + host + ":" + port + path;
                    properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
                } catch (Exception ignored) {}
            } else if (dbUrl.startsWith("jdbc:postgresql:")) {
                properties.setDriverClassName("org.postgresql.Driver");
            } else if (dbUrl.startsWith("jdbc:mysql:")) {
                properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
            }

            properties.setUrl(dbUrl);
        } else {
            // Local development default: MySQL on localhost
            properties.setUrl("jdbc:mysql://localhost:3306/nutrigen?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
            if (dbUser == null || dbUser.isBlank()) dbUser = "nutrigen";
            if (dbPass == null || dbPass.isBlank()) dbPass = "root";
        }

        if (dbUser != null && !dbUser.isBlank()) properties.setUsername(dbUser);
        if (dbPass != null && !dbPass.isBlank()) properties.setPassword(dbPass);

        return properties;
    }
}
