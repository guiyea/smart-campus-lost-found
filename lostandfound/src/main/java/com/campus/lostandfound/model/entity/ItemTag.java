package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物品标签实体类
 */
@Data
@TableName("item_tag")
public class ItemTag {
    
    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 物品ID
     */
    private Long itemId;
    
    /**
     * 标签名称
     */
    private String tag;
    
    /**
     * 置信度(0-1)
     */
    private BigDecimal confidence;
}
