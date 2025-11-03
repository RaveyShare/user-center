package com.ravey.ai.user.start.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Web配置类
 *
 * @author ravey
 * @since 1.0.0
 */
@Configuration
public class WebConfig {

    /**
     * RestTemplate Bean
     * 配置支持text/plain内容类型的JSON消息转换器，解决微信API返回text/plain但实际是JSON的问题
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 获取现有的消息转换器
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        
        // 创建支持text/plain的JSON转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN); // 添加对text/plain的支持
        supportedMediaTypes.add(new MediaType("application", "*+json"));
        jsonConverter.setSupportedMediaTypes(supportedMediaTypes);
        
        // 将新的转换器添加到列表开头，确保优先使用
        messageConverters.add(0, jsonConverter);
        
        return restTemplate;
    }


}