package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配记录实体类
 */
@Data
@TableName("match_record")
public class MatchRecord {
    
    /**
     * 匹配记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 失物信息ID
     */
    private Long lostItemId;
    
    /**
     * 招领信息ID
     */
    private Long foundItemId;
    
    /**
     * 匹配分数(0-100)
     */
    private BigDecimal score;
    
    /**
     * 状态: 0-待确认, 1-已确认, 2-已拒绝
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 确认时间
     */
    private LocalDateTime confirmedAt;
}
