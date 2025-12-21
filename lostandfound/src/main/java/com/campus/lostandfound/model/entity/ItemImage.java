package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品图片实体类
 */
@Data
@TableName("item_image")
public class ItemImage {
    
    /**
     * 图片ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 物品ID
     */
    private Long itemId;
    
    /**
     * 图片URL
     */
    private String url;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
