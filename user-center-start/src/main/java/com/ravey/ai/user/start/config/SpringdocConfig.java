package com.ravey.ai.user.start.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author ravey
 * @since 2024-10-15
 */
@Configuration
public class SpringdocConfig {

    @Bean
    public OpenAPI springShopOpenApi() {
        return new OpenAPI()
                // 接口文档标题
                .info(new Info().title("用户中心接口文档")
                        // 接口文档简介
                        .description("用户中心文档")
                        // 接口文档版本
                        .version("1.0.0")
                        // 开发者联系方式
                        .contact(new Contact().name("ravey")));

    }
}