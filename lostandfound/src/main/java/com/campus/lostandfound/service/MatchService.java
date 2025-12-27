package com.campus.lostandfound.service;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.MatchFeedbackDTO;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.vo.MatchResult;
import com.campus.lostandfound.model.vo.MatchVO;

import java.util.List;

/**
 * 匹配服务接口
 */
public interface MatchService {
    
    /**
     * 计算匹配
     * 
     * @param item 物品信息
     * @return 匹配结果列表
     */
    List<MatchResult> calculateMatch(Item item);
    
    /**
     * 获取匹配推荐
     * 
     * @param itemId 物品ID
     * @return 匹配推荐列表
     */
    Result<List<MatchVO>> getRecommendations(Long itemId);
    
    /**
     * 确认匹配
     * 
     * @param itemId 物品ID
     * @param matchedItemId 匹配的物品ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> confirmMatch(Long itemId, Long matchedItemId, Long userId);
    
    /**
     * 反馈匹配结果
     * 
     * @param dto 反馈信息
     * @return 操作结果
     */
    Result<Void> feedback(MatchFeedbackDTO dto);
    
    /**
     * 异步执行匹配计算（用于物品发布后的自动匹配）
     *
     * @param item 物品信息
     */
    void calculateMatchAsync(Item item);

    /**
     * 获取用户的匹配推荐列表
     * 返回用户所有待处理物品的匹配推荐（最多10条）
     *
     * @param userId 用户ID
     * @return 用户匹配推荐列表
     */
    Result<List<MatchVO>> getUserMatchRecommendations(Long userId);
}
