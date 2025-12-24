package com.campus.lostandfound.service;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.dto.SendMessageDTO;
import com.campus.lostandfound.model.vo.MessageVO;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface MessageService {
    
    /**
     * 发送消息
     * @param dto 发送消息DTO
     */
    void send(SendMessageDTO dto);
    
    /**
     * 批量发送消息
     * @param dtos 发送消息DTO列表
     */
    void sendBatch(List<SendMessageDTO> dtos);
    
    /**
     * 获取消息列表
     * @param userId 用户ID
     * @param type 消息类型（可选）
     * @param isRead 已读状态（可选）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页消息列表
     */
    PageResult<MessageVO> getList(Long userId, Integer type, Boolean isRead, Integer pageNum, Integer pageSize);
    
    /**
     * 标记消息已读
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markAsRead(Long messageId, Long userId);
    
    /**
     * 标记全部消息已读
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);
    
    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Integer getUnreadCount(Long userId);
}