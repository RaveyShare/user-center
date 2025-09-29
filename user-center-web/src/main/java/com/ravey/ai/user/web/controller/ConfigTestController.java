package com.ravey.ai.user.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置测试控制器，用于验证 Nacos 配置读取情况
 *
 * @author ravey
 * @since 1.0.0
 */
@RestController
@RequestMapping("/config-test")
public class ConfigTestController {

    @Value("${spring.datasource.url:未配置}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:未配置}")
    private String datasourceUsername;

    @Value("${common.mysql.url:未从Nacos读取}")
    private String commonMysqlUrl;

    @Value("${common.mysql.username:未从Nacos读取}")
    private String commonMysqlUsername;


    private final DataSource dataSource;

    public ConfigTestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取数据库配置信息
     */
    @GetMapping("/database-config")
    public Map<String, Object> getDatabaseConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 配置值
        config.put("datasourceUrl", datasourceUrl);
        config.put("datasourceUsername", datasourceUsername);
        config.put("commonMysqlUrl", commonMysqlUrl);
        config.put("commonMysqlUsername", commonMysqlUsername);
        
        // 实际连接信息
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            config.put("actualDatabaseUrl", metaData.getURL());
            config.put("actualDatabaseUsername", metaData.getUserName());
            config.put("databaseProductName", metaData.getDatabaseProductName());
            config.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            config.put("connectionStatus", "连接成功");
        } catch (Exception e) {
            config.put("connectionStatus", "连接失败: " + e.getMessage());
        }
        
        return config;
    }


    /**
     * 获取所有配置信息概览
     */
    @GetMapping("/all-config")
    public Map<String, Object> getAllConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("database", getDatabaseConfig());
        return result;
    }
}