package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.exception.ForbiddenException;
import com.campus.lostandfound.exception.NotFoundException;
import com.campus.lostandfound.model.dto.SendMessageDTO;
import com.campus.lostandfound.model.entity.Message;
import com.campus.lostandfound.model.vo.MessageVO;
import com.campus.lostandfound.repository.MessageMapper;
import com.campus.lostandfound.service.MessageService;
import com.campus.lostandfound.websocket.MessageWebSocket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public void send(SendMessageDTO dto) {
        log.info("发送消息给用户: {}, 标题: {}", dto.getUserId(), dto.getTitle());
        
        Message message = new Message();
        message.setUserId(dto.getUserId());
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setType(dto.getType());
        message.setRelatedId(dto.getRelatedId());
        message.setIsRead(0); // 未读
        message.setCreatedAt(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        log.info("消息发送成功, 消息ID: {}", message.getId());
        
        // 检查用户是否有活跃的WebSocket连接，如果有则实时推送消息
        pushMessageViaWebSocket(message);
    }
    
    /**
     * 通过WebSocket实时推送消息给用户
     * 
     * @param message 消息实体
     */
    private void pushMessageViaWebSocket(Message message) {
        Long userId = message.getUserId();
        
        // 检查用户是否在线
        if (!MessageWebSocket.isUserOnline(userId)) {
            log.debug("用户 {} 不在线，跳过WebSocket推送", userId);
            return;
        }
        
        try {
            // 将消息转换为VO
            MessageVO messageVO = convertToVO(message);
            
            // 序列化为JSON
            String jsonMessage = objectMapper.writeValueAsString(messageVO);
            
            // 通过WebSocket推送
            boolean sent = MessageWebSocket.sendToUser(userId, jsonMessage);
            
            if (sent) {
                log.info("WebSocket消息推送成功, 用户ID: {}, 消息ID: {}", userId, message.getId());
            } else {
                log.warn("WebSocket消息推送失败, 用户ID: {}, 消息ID: {}", userId, message.getId());
            }
        } catch (JsonProcessingException e) {
            log.error("消息序列化失败, 消息ID: {}, 错误: {}", message.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("WebSocket消息推送异常, 用户ID: {}, 消息ID: {}, 错误: {}", 
                    userId, message.getId(), e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void sendBatch(List<SendMessageDTO> dtos) {
        log.info("批量发送消息, 数量: {}", dtos.size());
        
        for (SendMessageDTO dto : dtos) {
            send(dto);
        }
        
        log.info("批量消息发送完成");
    }
    
    @Override
    public PageResult<MessageVO> getList(Long userId, Integer type, Boolean isRead, Integer pageNum, Integer pageSize) {
        log.info("获取用户消息列表: userId={}, type={}, isRead={}, pageNum={}, pageSize={}", 
                userId, type, isRead, pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        // 可选筛选条件
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        if (isRead != null) {
            queryWrapper.eq("is_read", isRead ? 1 : 0);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc("created_at");
        
        // 分页查询
        Page<Message> page = new Page<>(pageNum, pageSize);
        Page<Message> messagePage = messageMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<MessageVO> messageVOList = messagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(messageVOList, messagePage.getTotal(), pageNum, pageSize);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        log.info("标记消息已读: messageId={}, userId={}", messageId, userId);
        
        // 先查询消息是否存在且属于当前用户
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new NotFoundException("消息不存在");
        }
        
        if (!message.getUserId().equals(userId)) {
            throw new ForbiddenException("无权操作他人的消息");
        }
        
        // 更新已读状态
        UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", messageId)
                    .eq("user_id", userId)
                    .set("is_read", 1);
        
        int updated = messageMapper.update(null, updateWrapper);
        if (updated > 0) {
            log.info("消息标记已读成功: messageId={}", messageId);
        }
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("标记用户所有消息已读: userId={}", userId);
        
        UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                    .eq("is_read", 0)
                    .set("is_read", 1);
        
        int updated = messageMapper.update(null, updateWrapper);
        log.info("标记已读消息数量: {}", updated);
    }
    
    @Override
    public Integer getUnreadCount(Long userId) {
        log.debug("获取用户未读消息数量: userId={}", userId);
        
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_read", 0);
        
        Long count = messageMapper.selectCount(queryWrapper);
        return count.intValue();
    }
    
    /**
     * 将Message实体转换为MessageVO
     */
    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        
        // 转换已读状态
        vo.setIsRead(message.getIsRead() == 1);
        
        // 设置类型名称
        vo.setTypeNameByType();
        
        return vo;
    }
}