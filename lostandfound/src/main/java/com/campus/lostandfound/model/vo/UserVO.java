package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
public class UserVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 学号/工号
     */
    private String studentId;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 手机号（脱敏）
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 积分
     */
    private Integer points;
    
    /**
     * 角色：0-普通用户，1-管理员
     */
    private Integer role;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 脱敏手机号
     * 将手机号中间4位替换为****
     */
    public void maskPhone() {
        if (phone != null && phone.length() == 11) {
            this.phone = phone.substring(0, 3) + "****" + phone.substring(7);
        }
    }
}
