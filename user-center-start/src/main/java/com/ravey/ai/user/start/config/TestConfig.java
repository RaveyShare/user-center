package com.ravey.ai.user.start.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 测试配置类，用于验证 Nacos 配置读取
 * 
 * @author ravey
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "test")
public class TestConfig {
    
    private String message;
    private Integer number;
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getNumber() {
        return number;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
    
    @Override
    public String toString() {
        return "TestConfig{" +
                "message='" + message + '\'' +
                ", number=" + number +
                '}';
    }
}