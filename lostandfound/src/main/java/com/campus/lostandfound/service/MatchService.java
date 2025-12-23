package com.campus.lostandfound.service;

import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.vo.ItemVO;

import java.util.List;

/**
 * 匹配服务接口
 */
public interface MatchService {
    
    /**
     * 执行匹配计算
     * 
     * @param item 物品信息
     */
    void calculateMatchAsync(Item item);
    
    /**
     * 获取匹配推荐列表
     * 
     * @param itemId 物品ID
     * @return 匹配推荐列表（前10条）
     */
    List<ItemVO> getRecommendations(Long itemId);
}
