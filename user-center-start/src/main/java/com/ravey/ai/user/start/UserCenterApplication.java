package com.ravey.ai.user.start;

import com.ravey.common.service.web.annotation.EnableGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 *  用户中心启动类
 *
 * @author ravey
 * @since 2024-10-15
 */
@Slf4j
@EnableDiscoveryClient
@EnableGlobalExceptionHandler
@SpringBootApplication
@ComponentScan(value = {"com.ravey.ai.user"})
@MapperScan(value = {"com.ravey.ai.user.**.mapper"})
public class UserCenterApplication {

    public static void main(final String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
        log.info("========== user center is success ==========");
    }

}