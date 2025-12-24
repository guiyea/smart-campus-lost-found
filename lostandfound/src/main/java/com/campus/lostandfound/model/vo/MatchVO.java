package com.campus.lostandfound.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 匹配物品VO，继承ItemVO并增加匹配分数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MatchVO extends ItemVO {
    
    /**
     * 匹配分数 (0-100)
     */
    private BigDecimal matchScore;
    
    /**
     * 类别匹配分数 (0-30)
     */
    private BigDecimal categoryScore;
    
    /**
     * 标签匹配分数 (0-30)
     */
    private BigDecimal tagScore;
    
    /**
     * 时间匹配分数 (0-20)
     */
    private BigDecimal timeScore;
    
    /**
     * 位置匹配分数 (0-20)
     */
    private BigDecimal locationScore;
}