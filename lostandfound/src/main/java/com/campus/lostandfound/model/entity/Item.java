package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物品信息实体类
 */
@Data
@TableName("item")
public class Item {
    
    /**
     * 物品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发布用户ID
     */
    private Long userId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 类型: 0-失物, 1-招领
     */
    private Integer type;
    
    /**
     * 物品类别
     */
    private String category;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 地点描述
     */
    private String locationDesc;
    
    /**
     * 丢失/拾获时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 状态: 0-待处理, 1-已找回, 2-已关闭
     */
    private Integer status;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 删除标记: 0-未删除, 1-已删除 (软删除)
     */
    @TableLogic
    private Integer deleted;
    
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
