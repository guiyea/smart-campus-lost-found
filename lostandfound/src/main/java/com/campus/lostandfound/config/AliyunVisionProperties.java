package com.campus.lostandfound.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云视觉智能配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.vision")
public class AliyunVisionProperties {
    
    /**
     * 区域ID
     */
    private String regionId = "cn-shanghai";
    
    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 5000;
}
