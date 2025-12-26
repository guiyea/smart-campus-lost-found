package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ConfirmMatchDTO;
import com.campus.lostandfound.model.dto.MatchFeedbackDTO;
import com.campus.lostandfound.model.vo.MatchVO;
import com.campus.lostandfound.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 匹配控制器
 * 提供智能匹配推荐、匹配确认、匹配反馈等接口
 */
@Tag(name = "匹配管理", description = "智能匹配推荐、匹配确认、匹配反馈等接口")
@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {
    
    @Autowired
    private MatchService matchService;
    
    /**
     * 获取匹配推荐列表
     * 返回与指定物品匹配度最高的前10条物品信息
     * 
     * @param itemId 物品ID
     * @return 匹配推荐列表
     */
    @Operation(summary = "获取匹配推荐", description = "返回与指定物品匹配度最高的前10条物品信息，基于AI识别和关键词匹配")
    @GetMapping("/recommendations/{itemId}")
    public Result<List<MatchVO>> getRecommendations(
            @Parameter(description = "物品ID", required = true, example = "1")
            @PathVariable Long itemId) {
        // 调用Service获取匹配推荐
        Result<List<MatchVO>> result = matchService.getRecommendations(itemId);
        
        return result;
    }
    
    /**
     * 确认匹配
     * 用户确认两个物品匹配成功，更新双方状态为已找回
     * 
     * @param dto 确认匹配DTO，包含物品ID和匹配物品ID
     * @return 操作结果
     */
    @Operation(summary = "确认匹配", description = "用户确认两个物品匹配成功，更新双方状态为已找回，并触发积分奖励", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "匹配确认成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或物品不存在"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限操作该物品")
    })
    @PostMapping("/confirm")
    public Result<Void> confirmMatch(@Valid @RequestBody ConfirmMatchDTO dto) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        // 调用Service确认匹配
        Result<Void> result = matchService.confirmMatch(dto.getItemId(), dto.getMatchedItemId(), userId);
        
        return result;
    }
    
    /**
     * 提交匹配反馈
     * 用户对匹配结果进行反馈，用于优化匹配算法
     * 
     * @param dto 匹配反馈DTO
     * @return 操作结果
     */
    @Operation(summary = "提交匹配反馈", description = "用户对匹配结果进行反馈，用于优化匹配算法", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@Valid @RequestBody MatchFeedbackDTO dto) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        // 设置反馈用户ID
        dto.setUserId(userId);
        
        // 调用Service提交反馈
        Result<Void> result = matchService.feedback(dto);
        
        return result;
    }
}