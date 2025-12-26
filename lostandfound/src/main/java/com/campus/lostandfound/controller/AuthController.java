package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.LoginDTO;
import com.campus.lostandfound.model.dto.RegisterDTO;
import com.campus.lostandfound.model.vo.TokenVO;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户注册、登录、令牌刷新等认证相关接口
 */
@Tag(name = "认证管理", description = "用户注册、登录、令牌刷新等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户注册
     * 
     * @param dto 注册信息
     * @return 用户信息（不包含密码）
     */
    @Operation(summary = "用户注册", description = "新用户注册，需要提供学号、姓名、手机号和密码")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或学号已存在")
    })
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        UserVO userVO = authService.register(dto);
        return Result.success(userVO);
    }
    
    /**
     * 用户登录
     * 
     * @param dto 登录信息
     * @return 令牌信息
     */
    @Operation(summary = "用户登录", description = "使用学号和密码登录，返回访问令牌和刷新令牌")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = authService.login(dto);
        return Result.success(tokenVO);
    }
    
    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的令牌信息
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌和刷新令牌")
    @PostMapping("/refresh")
    public Result<TokenVO> refreshToken(
            @Parameter(description = "刷新令牌", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestBody String refreshToken) {
        TokenVO tokenVO = authService.refreshToken(refreshToken);
        return Result.success(tokenVO);
    }
}
