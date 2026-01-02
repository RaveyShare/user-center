package com.ravey.ai.user.start;

import com.ravey.common.service.web.annotation.EnableGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {"com.ravey.ai.user"})
@MapperScan("com.ravey.ai.user.service.dao.mapper")
@EnableGlobalExceptionHandler
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }
}