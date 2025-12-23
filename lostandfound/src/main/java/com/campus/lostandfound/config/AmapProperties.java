package com.campus.lostandfound.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapProperties {
    
    /**
     * 高德地图Web服务API Key
     */
    private String key;
    
    /**
     * 高德地图API基础URL
     */
    private String baseUrl = "https://restapi.amap.com/v3";
    
    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 3000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 5000;
}
