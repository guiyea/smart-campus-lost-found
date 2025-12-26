package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 物品详情VO
 * 继承ItemVO，增加匹配推荐列表
 */
@Schema(description = "物品详情信息")
@Data
@EqualsAndHashCode(callSuper = true)
public class ItemDetailVO extends ItemVO {
    
    /**
     * 匹配推荐列表
     */
    @Schema(description = "匹配推荐列表", accessMode = Schema.AccessMode.READ_ONLY)
    private List<MatchVO> matchRecommendations;
}
