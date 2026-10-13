package com.nutrigen.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DATABASE_URL");
        }

        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");
        String driverClassName;

        if (dbUrl != null && !dbUrl.isBlank()) {
            dbUrl = dbUrl.trim();

            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                driverClassName = "org.postgresql.Driver";
                int schemeEnd = dbUrl.indexOf("://");
                String rest = dbUrl.substring(schemeEnd + 3);

                if (rest.contains("@")) {
                    int atIndex = rest.lastIndexOf('@');
                    String userInfo = rest.substring(0, atIndex);
                    String hostAndRest = rest.substring(atIndex + 1);

                    if (hostAndRest.isBlank()) {
                        throw new IllegalStateException("Invalid database connection URL (" + sanitizeUrl(dbUrl) + "): host is missing");
                    }

                    int colonIndex = userInfo.indexOf(':');
                    if (colonIndex != -1) {
                        if (dbUser == null || dbUser.isBlank()) dbUser = decode(userInfo.substring(0, colonIndex));
                        if (dbPass == null || dbPass.isBlank()) dbPass = decode(userInfo.substring(colonIndex + 1));
                    } else {
                        if (dbUser == null || dbUser.isBlank()) dbUser = decode(userInfo);
                    }
                    dbUrl = "jdbc:postgresql://" + hostAndRest;
                } else {
                    dbUrl = "jdbc:postgresql://" + rest;
                }
            } else if (dbUrl.startsWith("mysql://")) {
                driverClassName = "com.mysql.cj.jdbc.Driver";
                int schemeEnd = dbUrl.indexOf("://");
                String rest = dbUrl.substring(schemeEnd + 3);

                if (rest.contains("@")) {
                    int atIndex = rest.lastIndexOf('@');
                    String userInfo = rest.substring(0, atIndex);
                    String hostAndRest = rest.substring(atIndex + 1);

                    if (hostAndRest.isBlank()) {
                        throw new IllegalStateException("Invalid database connection URL (" + sanitizeUrl(dbUrl) + "): host is missing");
                    }

                    int colonIndex = userInfo.indexOf(':');
                    if (colonIndex != -1) {
                        if (dbUser == null || dbUser.isBlank()) dbUser = decode(userInfo.substring(0, colonIndex));
                        if (dbPass == null || dbPass.isBlank()) dbPass = decode(userInfo.substring(colonIndex + 1));
                    } else {
                        if (dbUser == null || dbUser.isBlank()) dbUser = decode(userInfo);
                    }
                    dbUrl = "jdbc:mysql://" + hostAndRest;
                } else {
                    dbUrl = "jdbc:mysql://" + rest;
                }
            } else if (dbUrl.startsWith("jdbc:postgresql:")) {
                driverClassName = "org.postgresql.Driver";
            } else if (dbUrl.startsWith("jdbc:mysql:")) {
                driverClassName = "com.mysql.cj.jdbc.Driver";
            } else {
                throw new IllegalStateException("Unsupported database URL scheme (" + sanitizeUrl(dbUrl) + "). Expected postgresql://, postgres://, mysql://, or jdbc: URL.");
            }
        } else {
            // Local development default: MySQL on localhost
            dbUrl = "jdbc:mysql://localhost:3306/nutrigen?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            driverClassName = "com.mysql.cj.jdbc.Driver";
            if (dbUser == null || dbUser.isBlank()) dbUser = "nutrigen";
            if (dbPass == null || dbPass.isBlank()) dbPass = "root";
        }

        log.info("Configured primary DataSource: url={}, driver={}", sanitizeUrl(dbUrl), driverClassName);

        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .url(dbUrl)
                .driverClassName(driverClassName);

        if (dbUser != null && !dbUser.isBlank()) {
            builder.username(dbUser);
        }
        if (dbPass != null && !dbPass.isBlank()) {
            builder.password(dbPass);
        }

        return builder.build();
    }

    private static String decode(String val) {
        if (val == null) return null;
        try {
            // Protect literal '+' from being converted to a space by URLDecoder (RFC 3986 vs form-encoding)
            return URLDecoder.decode(val.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return val;
        }
    }

    private static String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) return "<empty>";
        return url.replaceAll("://([^:@]+)(?::([^@]+))?@", "://***:***@");
    }
}
