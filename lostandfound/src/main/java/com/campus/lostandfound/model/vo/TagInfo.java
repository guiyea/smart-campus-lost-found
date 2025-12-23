package com.campus.lostandfound.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 图像识别标签信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagInfo {
    
    /**
     * 标签名称
     */
    private String tag;
    
    /**
     * 置信度 (0-1)
     */
    private BigDecimal confidence;
}
