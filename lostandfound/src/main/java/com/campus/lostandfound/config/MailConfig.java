package com.campus.lostandfound.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件服务配置类
 * 配置JavaMailSender Bean用于发送邮件
 * 
 * 注意：只有当spring.mail.host配置存在时才会启用此配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MailProperties.class)
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MailConfig {

    /**
     * 创建JavaMailSender Bean
     * Spring Boot会自动配置JavaMailSender，但这里显式配置以便于自定义和日志记录
     *
     * @param mailProperties 邮件配置属性（由Spring Boot自动注入）
     * @return JavaMailSender实例
     */
    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());
        
        if (mailProperties.getDefaultEncoding() != null) {
            mailSender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        }
        
        Properties props = mailSender.getJavaMailProperties();
        
        // 从配置中获取SMTP属性
        if (mailProperties.getProperties() != null) {
            props.putAll(mailProperties.getProperties());
        }
        
        log.info("初始化邮件服务，host: {}, port: {}", 
                mailProperties.getHost(), 
                mailProperties.getPort());
        
        return mailSender;
    }
}
