package com.ravey.ai.user.start;

import com.ravey.ai.user.start.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

/**
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {"com.ravey.ai.user"})
@MapperScan("com.ravey.ai.user.service.dao.mapper")
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }

//    /**
//     * 应用启动后打印配置信息，用于调试 Nacos 配置读取情况
//     */
//    @Bean
//    public ApplicationRunner configDebugRunner(Environment environment, TestConfig testConfig) {
//        return args -> {
//            log.info("=== Nacos 配置调试信息 ===");
//
//            // 检查 Nacos 相关配置
//            String nacosServerAddr = environment.getProperty("spring.cloud.nacos.config.server-addr");
//            String nacosNamespace = environment.getProperty("spring.cloud.nacos.config.namespace");
//            String nacosGroup = environment.getProperty("spring.cloud.nacos.config.group");
//            String nacosUsername = environment.getProperty("spring.cloud.nacos.config.username");
//
//            log.info("Nacos Server Address: {}", nacosServerAddr);
//            log.info("Nacos Namespace: {}", nacosNamespace);
//            log.info("Nacos Group: {}", nacosGroup);
//            log.info("Nacos Username: {}", nacosUsername);
//
//            // 检查数据库配置
//            String datasourceUrl = environment.getProperty("spring.datasource.url");
//            String datasourceUsername = environment.getProperty("spring.datasource.username");
//            String commonMysqlUrl = environment.getProperty("common.mysql.url");
//            String commonMysqlUsername = environment.getProperty("common.mysql.username");
//
//            log.info("=== 数据库配置信息 ===");
//            log.info("spring.datasource.url: {}", datasourceUrl);
//            log.info("spring.datasource.username: {}", datasourceUsername);
//            log.info("common.mysql.url: {}", commonMysqlUrl);
//            log.info("common.mysql.username: {}", commonMysqlUsername);
//
//            // 检查 Redis 配置
//            String redisHost = environment.getProperty("spring.data.redis.host");
//            String redisDatabase = environment.getProperty("spring.data.redis.database");
//            String commonRedisHost = environment.getProperty("common.redis.host");
//
//            log.info("=== Redis 配置信息 ===");
//            log.info("spring.data.redis.host: {}", redisHost);
//            log.info("spring.data.redis.database: {}", redisDatabase);
//            log.info("common.redis.host: {}", commonRedisHost);
//
//            // 检查测试配置（验证 Nacos 配置读取）
//            log.info("=== 测试配置验证（Nacos 配置读取验证）===");
//            log.info("test.message: {}", testConfig.getMessage());
//            log.info("test.number: {}", testConfig.getNumber());
//            log.info("TestConfig 对象: {}", testConfig);
//
//            // 验证配置是否成功读取
//            if (testConfig.getMessage() != null && testConfig.getNumber() != null) {
//                log.info("✅ 成功从 Nacos 读取到测试配置！");
//                log.info("✅ 配置内容验证：message='{}', number={}", testConfig.getMessage(), testConfig.getNumber());
//            } else {
//                log.warn("❌ 未能从 Nacos 读取到测试配置，配置可能未正确加载");
//            }
//
//            // 检查活跃的配置文件
//            String[] activeProfiles = environment.getActiveProfiles();
//            log.info("=== 活跃的配置文件 ===");
//            for (String profile : activeProfiles) {
//                log.info("Active Profile: {}", profile);
//            }
//
//            log.info("=== 配置调试信息结束 ===");
//        };
//    }
}