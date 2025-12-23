package com.campus.lostandfound.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 物品详情VO
 * 继承ItemVO，增加匹配推荐列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ItemDetailVO extends ItemVO {
    
    /**
     * 匹配推荐列表
     */
    private List<ItemVO> matchRecommendations;
}
