package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 匹配物品VO，继承ItemVO并增加匹配分数
 */
@Schema(description = "匹配物品信息")
@Data
@EqualsAndHashCode(callSuper = true)
public class MatchVO extends ItemVO {
    
    /**
     * 匹配分数 (0-100)
     */
    @Schema(description = "匹配分数 (0-100)", example = "85.5", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal matchScore;
    
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
}