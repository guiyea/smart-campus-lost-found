package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录实体类
 */
@Data
@TableName("point_record")
public class PointRecord {
    
    /**
     * 积分记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分变动(正数为增加,负数为减少)
     */
    private Integer points;
    
    /**
     * 变动原因
     */
    private String reason;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    private Long relatedId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
