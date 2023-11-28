package com.example.pomelo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private static String driverName;
    private static String url;
    private static String username;
    private static String password;

    @Value("${database.driver}")
    public void setDriverName(String driver) {
        DataSourceConfig.driverName = driver;
    }

    @Value("${database.url}")
    public void setUrl(String url) {
        DataSourceConfig.url = url;
    }

    @Value("${database.username}")
    public void setUsername(String username) {
        DataSourceConfig.username = username;
    }

    @Value("${database.password}")
    public void setPassword(String password) {
        DataSourceConfig.password = password;
    }

    @Bean
    public static DataSource source() {
        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        builder.driverClassName(driverName);
        builder.url(url);
        builder.username(username);
        builder.password(password);
        return builder.build();
    }
}
