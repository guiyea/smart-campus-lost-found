package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostandfound.exception.BusinessException;
import com.campus.lostandfound.exception.ForbiddenException;
import com.campus.lostandfound.exception.UnauthorizedException;
import com.campus.lostandfound.model.dto.LoginDTO;
import com.campus.lostandfound.model.dto.RegisterDTO;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.TokenVO;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.AuthService;
import com.campus.lostandfound.util.JwtUtil;
import com.campus.lostandfound.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;
    
    /**
     * 用户注册
     * 
     * @param dto 注册信息
     * @return 用户信息VO（不包含密码）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        // 验证学号是否已存在
        LambdaQueryWrapper<User> studentIdQuery = new LambdaQueryWrapper<>();
        studentIdQuery.eq(User::getStudentId, dto.getStudentId());
        Long studentIdCount = userMapper.selectCount(studentIdQuery);
        if (studentIdCount > 0) {
            throw new BusinessException("学号已存在");
        }
        
        // 验证手机号是否已存在
        LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
        phoneQuery.eq(User::getPhone, dto.getPhone());
        Long phoneCount = userMapper.selectCount(phoneQuery);
        if (phoneCount > 0) {
            throw new BusinessException("手机号已被注册");
        }
        
        // 创建用户实体
        User user = new User();
        user.setStudentId(dto.getStudentId());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        
        // 使用BCrypt加密密码
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);
        
        // 设置默认值
        user.setPoints(0);           // 默认积分为0
        user.setRole(0);             // 默认角色为0（普通用户）
        user.setStatus(0);           // 默认状态为0（正常）
        
        // 保存用户到数据库
        userMapper.insert(user);
        
        // 转换为UserVO返回（不包含密码）
        return convertToUserVO(user);
    }
    
    /**
     * 用户登录
     * 
     * @param dto 登录信息
     * @return 令牌信息
     */
    @Override
    public TokenVO login(LoginDTO dto) {
        // 根据studentId查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStudentId, dto.getStudentId());
        User user = userMapper.selectOne(queryWrapper);
        
        // 用户不存在
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == 1) {
            throw new ForbiddenException("账户已被封禁");
        }
        
        // 检查登录失败次数
        String failKey = "login:fail:" + dto.getStudentId();
        String failCountStr = redisUtil.get(failKey);
        int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        
        // 如果失败次数>=5，检查锁定时间
        if (failCount >= 5) {
            throw new BusinessException("账户已锁定，请15分钟后重试");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            // 密码错误，增加失败次数
            redisUtil.increment(failKey);
            redisUtil.expire(failKey, 15, TimeUnit.MINUTES);
            throw new UnauthorizedException("密码错误");
        }
        
        // 密码正确，清除失败次数
        redisUtil.delete(failKey);
        
        // 检查是否为当日首次登录
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dailyLoginKey = "login:daily:" + user.getId() + ":" + today;
        
        if (!Boolean.TRUE.equals(redisUtil.hasKey(dailyLoginKey))) {
            // 首次登录，设置标记（过期时间到当天结束）
            redisUtil.set(dailyLoginKey, "1", 1, TimeUnit.DAYS);
            
            // TODO: 调用PointService增加2积分
            // pointService.addPoints(user.getId(), 2, "DAILY_LOGIN", null);
        }
        
        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getStudentId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        // 构建TokenVO
        TokenVO tokenVO = new TokenVO();
        tokenVO.setAccessToken(accessToken);
        tokenVO.setRefreshToken(refreshToken);
        tokenVO.setExpiresIn(accessTokenExpiration / 1000); // 转换为秒
        tokenVO.setUserInfo(convertToUserVO(user));
        
        return tokenVO;
    }
    
    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的令牌信息
     */
    @Override
    public TokenVO refreshToken(String refreshToken) {
        // 验证refreshToken有效性
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("刷新令牌无效或已过期");
        }
        
        // 从refreshToken提取userId
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == 1) {
            throw new ForbiddenException("账户已被封禁");
        }
        
        // 生成新的accessToken和refreshToken
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getStudentId(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        // 构建TokenVO
        TokenVO tokenVO = new TokenVO();
        tokenVO.setAccessToken(newAccessToken);
        tokenVO.setRefreshToken(newRefreshToken);
        tokenVO.setExpiresIn(accessTokenExpiration / 1000); // 转换为秒
        tokenVO.setUserInfo(convertToUserVO(user));
        
        return tokenVO;
    }
    
    /**
     * 退出登录
     * 
     * @param token 访问令牌
     */
    @Override
    public void logout(String token) {
        // TODO: 实现退出登录逻辑
        throw new UnsupportedOperationException("退出登录功能尚未实现");
    }
    
    /**
     * 将User实体转换为UserVO
     * 
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 脱敏手机号
        userVO.maskPhone();
        return userVO;
    }
}
