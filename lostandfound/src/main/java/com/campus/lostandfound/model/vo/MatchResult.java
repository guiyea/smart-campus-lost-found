package com.campus.lostandfound.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 匹配结果VO
 */
@Data
public class MatchResult {
    
    /**
     * 物品ID
     */
    private Long itemId;
    
    /**
     * 匹配的物品ID
     */
    private Long matchedItemId;
    
    /**
     * 总匹配分数 (0-100)
     */
    private BigDecimal score;
    
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
    
    public MatchResult() {}
    
    public MatchResult(Long itemId, Long matchedItemId) {
        this.itemId = itemId;
        this.matchedItemId = matchedItemId;
        this.score = BigDecimal.ZERO;
        this.categoryScore = BigDecimal.ZERO;
        this.tagScore = BigDecimal.ZERO;
        this.timeScore = BigDecimal.ZERO;
        this.locationScore = BigDecimal.ZERO;
    }
}