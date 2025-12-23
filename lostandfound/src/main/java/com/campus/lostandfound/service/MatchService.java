package com.campus.lostandfound.service;

import com.campus.lostandfound.model.entity.Item;

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
}
