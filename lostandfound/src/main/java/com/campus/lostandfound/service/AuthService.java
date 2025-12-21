package com.campus.lostandfound.service;

import com.campus.lostandfound.model.dto.LoginDTO;
import com.campus.lostandfound.model.dto.RegisterDTO;
import com.campus.lostandfound.model.vo.TokenVO;
import com.campus.lostandfound.model.vo.UserVO;

/**
 * 认证服务接口
 * 提供用户注册、登录、令牌刷新和退出登录功能
 */
public interface AuthService {
    
    /**
     * 用户注册
     * 
     * @param dto 注册信息
     * @return 用户信息VO（不包含密码）
     */
    UserVO register(RegisterDTO dto);
    
    /**
     * 用户登录
     * 
     * @param dto 登录信息
     * @return 令牌信息
     */
    TokenVO login(LoginDTO dto);
    
    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的令牌信息
     */
    TokenVO refreshToken(String refreshToken);
    
    /**
     * 退出登录
     * 
     * @param token 访问令牌
     */
    void logout(String token);
}
