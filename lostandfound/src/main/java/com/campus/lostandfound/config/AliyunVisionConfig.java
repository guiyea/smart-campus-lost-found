package com.campus.lostandfound.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云视觉智能配置类
 * 复用OSS的accessKey配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AliyunVisionConfig {
    
    private final OssProperties ossProperties;
    private final AliyunVisionProperties visionProperties;
    
    /**
     * 创建阿里云SDK客户端Bean
     * 用于调用视觉智能API
     * 
     * @return IAcsClient实例
     */
    @Bean
    public IAcsClient acsClient() {
        log.info("初始化阿里云视觉智能客户端，regionId: {}", visionProperties.getRegionId());
        
        // 使用OSS的accessKey配置
        DefaultProfile profile = DefaultProfile.getProfile(
                visionProperties.getRegionId(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        
        // 设置超时时间
        profile.getHttpClientConfig().setConnectionTimeoutMillis(visionProperties.getTimeout());
        profile.getHttpClientConfig().setReadTimeoutMillis(visionProperties.getTimeout());
        
        return new DefaultAcsClient(profile);
    }
}
