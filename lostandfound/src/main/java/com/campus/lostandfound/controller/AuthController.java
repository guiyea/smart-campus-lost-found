package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.LoginDTO;
import com.campus.lostandfound.model.dto.RegisterDTO;
import com.campus.lostandfound.model.vo.TokenVO;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户注册、登录、令牌刷新等认证相关接口
 */
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
    @PostMapping("/refresh")
    public Result<TokenVO> refreshToken(@RequestBody String refreshToken) {
        TokenVO tokenVO = authService.refreshToken(refreshToken);
        return Result.success(tokenVO);
    }
}
