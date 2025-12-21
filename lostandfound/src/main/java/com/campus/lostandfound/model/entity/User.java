package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
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
     * 手机号
     */
    private String phone;
    
    /**
     * 密码(BCrypt加密)
     */
    private String password;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 积分
     */
    private Integer points;
    
    /**
     * 角色: 0-普通用户, 1-管理员
     */
    private Integer role;
    
    /**
     * 状态: 0-正常, 1-封禁
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
