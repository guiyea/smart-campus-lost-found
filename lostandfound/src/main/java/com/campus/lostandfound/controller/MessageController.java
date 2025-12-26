package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.vo.MessageVO;
import com.campus.lostandfound.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 * 提供消息列表查询、已读标记等接口
 */
@Tag(name = "消息管理", description = "消息列表查询、已读标记等接口")
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    /**
     * 获取当前用户的消息列表
     * 
     * @param type 消息类型（可选）: 0-系统通知, 1-匹配通知, 2-留言通知
     * @param isRead 已读状态（可选）: true-已读, false-未读
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认20
     * @return 分页消息列表
     */
    @Operation(summary = "获取消息列表", description = "获取当前用户的消息列表，支持按类型和已读状态筛选", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public Result<PageResult<MessageVO>> getMessageList(
            @Parameter(description = "消息类型：0-系统通知, 1-匹配通知, 2-留言通知", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "已读状态：true-已读, false-未读", example = "false")
            @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        // 从SecurityContext获取当前用户ID
        Long userId = getCurrentUserId();
        
        PageResult<MessageVO> pageResult = messageService.getList(userId, type, isRead, pageNum, pageSize);
        return Result.success(pageResult);
    }
    
    /**
     * 获取当前用户的未读消息数量
     * 
     * @return 未读消息数量
     */
    @Operation(summary = "获取未读消息数量", description = "获取当前用户的未读消息数量", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        // 从SecurityContext获取当前用户ID
        Long userId = getCurrentUserId();
        
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    /**
     * 标记单条消息为已读
     * 
     * @param id 消息ID
     * @return 操作结果
     */
    @Operation(summary = "标记消息为已读", description = "标记单条消息为已读状态", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID", required = true, example = "1")
            @PathVariable Long id) {
        // 从SecurityContext获取当前用户ID
        Long userId = getCurrentUserId();
        
        messageService.markAsRead(id, userId);
        return Result.success("消息已标记为已读", null);
    }
    
    /**
     * 标记当前用户的所有消息为已读
     * 
     * @return 操作结果
     */
    @Operation(summary = "标记所有消息为已读", description = "标记当前用户的所有消息为已读状态", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        // 从SecurityContext获取当前用户ID
        Long userId = getCurrentUserId();
        
        messageService.markAllAsRead(userId);
        return Result.success("所有消息已标记为已读", null);
    }
    
    /**
     * 从SecurityContext获取当前登录用户ID
     * 
     * @return 当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
