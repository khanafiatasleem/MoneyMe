package com.moneyme.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:}")
    private String defaultUrl;

    @Value("${spring.datasource.username:postgres}")
    private String defaultUsername;

    @Value("${spring.datasource.password:postgres}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        // If testing with H2 in-memory
        if (defaultUrl != null && defaultUrl.startsWith("jdbc:h2")) {
            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl(defaultUrl);
            h2Config.setUsername(defaultUsername.isEmpty() ? "sa" : defaultUsername);
            h2Config.setPassword(defaultPassword);
            h2Config.setDriverClassName("org.h2.Driver");
            return new HikariDataSource(h2Config);
        }

        // Check for Render/Heroku standard DATABASE_URL
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl == null || envDbUrl.trim().isEmpty()) {
            envDbUrl = System.getenv("DB_URL");
        }

        HikariConfig config = new HikariConfig();

        if (envDbUrl != null && (envDbUrl.startsWith("postgres://") || envDbUrl.startsWith("postgresql://"))) {
            log.info("Detected cloud PostgreSQL URI, transforming to JDBC format...");
            try {
                String cleanUriStr = envDbUrl.replace("jdbc:", "");
                URI uri = new URI(cleanUriStr);

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();

                String userInfo = uri.getUserInfo();
                String username = defaultUsername;
                String password = defaultPassword;

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (uri.getQuery() != null) {
                    jdbcUrl += "?" + uri.getQuery();
                }

                log.info("Configured JDBC URL: jdbc:postgresql://{}:{}{}", host, port, path);
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
            } catch (Exception e) {
                log.warn("Failed to parse DATABASE_URL as URI, falling back to raw property: {}", e.getMessage());
                config.setJdbcUrl(defaultUrl);
                config.setUsername(defaultUsername);
                config.setPassword(defaultPassword);
            }
        } else {
            // Standard local or direct JDBC URL
            config.setJdbcUrl(defaultUrl == null || defaultUrl.isEmpty() ? "jdbc:postgresql://localhost:5432/moneyme_db" : defaultUrl);
            config.setUsername(defaultUsername);
            config.setPassword(defaultPassword);
            if (driverClassName != null && !driverClassName.isEmpty()) {
                config.setDriverClassName(driverClassName);
            }
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);

        return new HikariDataSource(config);
    }
}
