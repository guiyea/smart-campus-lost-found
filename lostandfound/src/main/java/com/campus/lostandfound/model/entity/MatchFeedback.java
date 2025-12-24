package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 匹配反馈实体类
 */
@Data
@TableName("match_feedback")
public class MatchFeedback {
    
    /**
     * 反馈ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 物品ID
     */
    private Long itemId;
    
    /**
     * 匹配的物品ID
     */
    private Long matchedItemId;
    
    /**
     * 反馈用户ID
     */
    private Long userId;
    
    /**
     * 是否准确匹配
     */
    private Boolean isAccurate;
    
    /**
     * 反馈评论
     */
    private String comment;
    
    /**
     * 匹配分数（记录当时的匹配分数）
     */
    private java.math.BigDecimal matchScore;
    
    /**
     * 物品类别（用于算法优化）
     */
    private String itemCategory;
    
    /**
     * 匹配物品类别（用于算法优化）
     */
    private String matchedItemCategory;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}