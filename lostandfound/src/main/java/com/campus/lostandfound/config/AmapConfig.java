package com.campus.lostandfound.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 高德地图配置类
 * 用于调用高德地图Web服务API
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AmapConfig {
    
    private final AmapProperties amapProperties;
    
    /**
     * 创建用于调用高德地图API的RestTemplate Bean
     * 
     * @return RestTemplate实例
     */
    @Bean(name = "amapRestTemplate")
    public RestTemplate amapRestTemplate() {
        log.info("初始化高德地图RestTemplate，baseUrl: {}, connectTimeout: {}ms, readTimeout: {}ms",
                amapProperties.getBaseUrl(),
                amapProperties.getConnectTimeout(),
                amapProperties.getReadTimeout());
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(amapProperties.getConnectTimeout());
        factory.setReadTimeout(amapProperties.getReadTimeout());
        
        return new RestTemplate(factory);
    }
}
