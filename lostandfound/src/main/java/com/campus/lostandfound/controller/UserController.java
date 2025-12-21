package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.UpdateProfileDTO;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.service.UserService;
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
     * 支持按学号和姓名筛选
     * 
     * @param studentId 学号（可选，支持模糊查询）
     * @param name 姓名（可选，支持模糊查询）
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认20
     * @return 分页用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        PageResult<UserVO> pageResult = userService.getUserList(studentId, name, pageNum, pageSize);
        return Result.success(pageResult);
    }
}
