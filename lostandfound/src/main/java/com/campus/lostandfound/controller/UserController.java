package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.UpdateProfileDTO;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 提供用户信息查询、更新等接口
 */
@Tag(name = "用户管理", description = "用户信息查询、更新等接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前登录用户信息
     * 从SecurityContext获取userId
     * 
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        UserVO userVO = userService.getProfile(userId);
        return Result.success(userVO);
    }
    
    /**
     * 更新当前用户信息
     * 
     * @param dto 更新信息DTO
     * @return 更新后的用户信息
     */
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的个人信息，如姓名、手机号、头像等", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me")
    public Result<UserVO> updateCurrentUser(@Valid @RequestBody UpdateProfileDTO dto) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        UserVO userVO = userService.updateProfile(userId, dto);
        return Result.success(userVO);
    }
    
    /**
     * 分页查询用户列表（管理员功能）
     * 支持按学号、姓名和状态筛选
     * 
     * @param studentId 学号（可选，支持模糊查询）
     * @param name 姓名（可选，支持模糊查询）
     * @param status 状态（可选，0-正常，1-封禁）
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认20
     * @return 分页用户列表
     */
    @Operation(summary = "查询用户列表", description = "管理员功能：分页查询用户列表，支持按学号、姓名和状态筛选", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserVO>> getUserList(
            @Parameter(description = "学号（支持模糊查询）", example = "2021001")
            @RequestParam(required = false) String studentId,
            @Parameter(description = "姓名（支持模糊查询）", example = "张三")
            @RequestParam(required = false) String name,
            @Parameter(description = "状态：0-正常，1-封禁", example = "0")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        PageResult<UserVO> pageResult = userService.getUserList(studentId, name, status, pageNum, pageSize);
        return Result.success(pageResult);
    }
}
