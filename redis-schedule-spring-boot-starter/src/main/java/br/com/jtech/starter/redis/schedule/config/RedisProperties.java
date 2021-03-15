package br.com.jtech.starter.redis.schedule.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("spring.redis")
public class RedisProperties {
    private Integer database;
    private String host;
    private Integer port;
    private String password;

    private Integer maxActive = 8;

    private Integer maxIdle = 8;

    private Integer minIdle = 0;
}