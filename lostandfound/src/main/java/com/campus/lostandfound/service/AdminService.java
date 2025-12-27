package com.campus.lostandfound.service;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.dto.ItemAdminSearchDTO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.model.vo.StatisticsVO;

import java.time.LocalDate;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 获取平台统计数据
     * 包括用户总数、信息总数、匹配成功数、匹配成功率、今日新增、近7天趋势等
     * 
     * @return 统计数据VO
     */
    StatisticsVO getStatistics();
    
    /**
     * 获取管理员物品列表
     * 支持查询所有物品（包括已删除的），支持按状态、时间、举报次数筛选
     * 
     * @param dto 搜索条件
     * @return 分页物品列表（包含发布者信息）
     */
    PageResult<ItemVO> getItemList(ItemAdminSearchDTO dto);
    
    /**
     * 审核物品信息
     * 管理员对被举报或需要审核的物品进行处理
     * 
     * @param itemId 物品ID
     * @param action 审核操作: 0-通过, 1-删除, 2-警告发布者
     * @param reason 审核原因/备注
     */
    void reviewItem(Long itemId, Integer action, String reason);
    
    /**
     * 恢复已删除的物品
     * 将物品的deleted状态从1改为0
     * 
     * @param itemId 物品ID
     */
    void restoreItem(Long itemId);
    
    /**
     * 封禁用户
     * 更新用户状态为封禁，并向用户发送封禁通知消息
     * 
     * @param userId 用户ID
     * @param reason 封禁原因
     */
    void banUser(Long userId, String reason);
    
    /**
     * 解封用户
     * 更新用户状态为正常，并向用户发送解封通知消息
     * 
     * @param userId 用户ID
     */
    void unbanUser(Long userId);
    
    /**
     * 导出数据报表
     * 生成包含指定时间范围内运营数据的Excel文件
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    byte[] exportReport(LocalDate startDate, LocalDate endDate);
}
