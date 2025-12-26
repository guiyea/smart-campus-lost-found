package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 匹配结果VO
 */
@Schema(description = "匹配结果")
@Data
public class MatchResult {
    
    /**
     * 物品ID
     */
    @Schema(description = "物品ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long itemId;
    
    /**
     * 匹配的物品ID
     */
    @Schema(description = "匹配的物品ID", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
    private Long matchedItemId;
    
    /**
     * 总匹配分数 (0-100)
     */
    @Schema(description = "总匹配分数 (0-100)", example = "85.5", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal score;
    
    /**
     * 类别匹配分数 (0-30)
     */
    @Schema(description = "类别匹配分数 (0-30)", example = "25.0", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal categoryScore;
    
    /**
     * 标签匹配分数 (0-30)
     */
    @Schema(description = "标签匹配分数 (0-30)", example = "20.0", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal tagScore;
    
    /**
     * 时间匹配分数 (0-20)
     */
    @Schema(description = "时间匹配分数 (0-20)", example = "15.0", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal timeScore;
    
    /**
     * 位置匹配分数 (0-20)
     */
    @Schema(description = "位置匹配分数 (0-20)", example = "18.0", accessMode = Schema.AccessMode.READ_ONLY)
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