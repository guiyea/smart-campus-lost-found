package com.campus.lostandfound.service;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.vo.PointRankVO;
import com.campus.lostandfound.model.vo.PointRecordVO;

import java.util.List;

/**
 * 积分服务接口
 */
public interface PointService {
    
    /**
     * 增加用户积分
     * 
     * @param userId 用户ID
     * @param points 积分数量
     * @param reason 原因
     * @param relatedId 关联ID
     */
    void addPoints(Long userId, Integer points, String reason, Long relatedId);
    
    /**
     * 获取用户积分明细
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的积分记录列表
     */
    PageResult<PointRecordVO> getRecords(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取积分排行榜
     * 
     * @param limit 返回数量限制（最大100）
     * @return 积分排行榜列表
     */
    List<PointRankVO> getRanking(Integer limit);
    
    /**
     * 获取用户总积分
     * 
     * @param userId 用户ID
     * @return 用户总积分
     */
    Integer getTotalPoints(Long userId);
}
