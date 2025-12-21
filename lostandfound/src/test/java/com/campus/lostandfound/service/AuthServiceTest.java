package com.campus.lostandfound.service;

import com.campus.lostandfound.exception.BusinessException;
import com.campus.lostandfound.exception.ForbiddenException;
import com.campus.lostandfound.exception.UnauthorizedException;
import com.campus.lostandfound.model.dto.LoginDTO;
import com.campus.lostandfound.model.dto.RegisterDTO;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.TokenVO;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.impl.AuthServiceImpl;
import com.campus.lostandfound.util.JwtUtil;
import com.campus.lostandfound.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试类
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private RedisUtil redisUtil;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private RegisterDTO validRegisterDTO;
    private LoginDTO validLoginDTO;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        validRegisterDTO = new RegisterDTO();
        validRegisterDTO.setStudentId("2021001");
        validRegisterDTO.setName("张三");
        validRegisterDTO.setPhone("13800138000");
        validRegisterDTO.setPassword("password123");
        
        validLoginDTO = new LoginDTO();
        validLoginDTO.setStudentId("2021001");
        validLoginDTO.setPassword("password123");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setStudentId("2021001");
        testUser.setName("张三");
        testUser.setPhone("13800138000");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setPoints(0);
        testUser.setRole(0);
        testUser.setStatus(0);
        
        // Set the accessTokenExpiration field
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 7200000L);
    }
    
    @Test
    void testRegister_Success() {
        // 模拟学号和手机号不存在
        when(userMapper.selectCount(any())).thenReturn(0L);
        
        // 模拟密码加密
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        
        // 模拟插入成功
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        
        // 执行注册
        UserVO result = authService.register(validRegisterDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(validRegisterDTO.getStudentId(), result.getStudentId());
        assertEquals(validRegisterDTO.getName(), result.getName());
        assertEquals("138****8000", result.getPhone()); // 验证手机号脱敏
        assertEquals(0, result.getPoints()); // 验证默认积分为0
        assertEquals(0, result.getRole()); // 验证默认角色为0
        
        // 验证方法调用
        verify(userMapper, times(2)).selectCount(any()); // 验证学号和手机号
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userMapper, times(1)).insert(any(User.class));
    }
    
    @Test
    void testRegister_StudentIdExists() {
        // 模拟学号已存在
        when(userMapper.selectCount(any())).thenReturn(1L);
        
        // 执行注册并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(validRegisterDTO);
        });
        
        assertEquals("学号已存在", exception.getMessage());
        
        // 验证只调用了一次selectCount（检查学号时就抛出异常）
        verify(userMapper, times(1)).selectCount(any());
        verify(userMapper, never()).insert(any(User.class));
    }
    
    @Test
    void testRegister_PhoneExists() {
        // 模拟学号不存在，但手机号已存在
        when(userMapper.selectCount(any()))
            .thenReturn(0L)  // 第一次调用：学号不存在
            .thenReturn(1L); // 第二次调用：手机号已存在
        
        // 执行注册并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(validRegisterDTO);
        });
        
        assertEquals("手机号已被注册", exception.getMessage());
        
        // 验证调用了两次selectCount（检查学号和手机号）
        verify(userMapper, times(2)).selectCount(any());
        verify(userMapper, never()).insert(any(User.class));
    }
    
    @Test
    void testRegister_PasswordEncryption() {
        // 模拟学号和手机号不存在
        when(userMapper.selectCount(any())).thenReturn(0L);
        
        // 模拟密码加密
        String encodedPassword = "$2a$10$encodedPassword";
        when(passwordEncoder.encode(validRegisterDTO.getPassword())).thenReturn(encodedPassword);
        
        // 模拟插入成功
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // 验证密码已加密
            assertEquals(encodedPassword, user.getPassword());
            user.setId(1L);
            return 1;
        });
        
        // 执行注册
        authService.register(validRegisterDTO);
        
        // 验证密码加密方法被调用
        verify(passwordEncoder, times(1)).encode(validRegisterDTO.getPassword());
    }
    
    @Test
    void testLogin_Success() {
        // 模拟查询用户成功
        when(userMapper.selectOne(any())).thenReturn(testUser);
        
        // 模拟Redis中没有失败记录
        when(redisUtil.get(anyString())).thenReturn(null);
        
        // 模拟密码验证成功
        when(passwordEncoder.matches(validLoginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        
        // 模拟Redis检查首次登录
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        
        // 模拟JWT生成
        when(jwtUtil.generateAccessToken(testUser.getId(), testUser.getStudentId(), testUser.getRole()))
            .thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(testUser.getId())).thenReturn("refresh-token");
        
        // 执行登录
        TokenVO result = authService.login(validLoginDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals(7200L, result.getExpiresIn()); // 7200000ms / 1000 = 7200s
        assertNotNull(result.getUserInfo());
        assertEquals(testUser.getStudentId(), result.getUserInfo().getStudentId());
        
        // 验证方法调用
        verify(userMapper, times(1)).selectOne(any());
        verify(passwordEncoder, times(1)).matches(validLoginDTO.getPassword(), testUser.getPassword());
        verify(redisUtil, times(1)).delete(anyString()); // 清除失败次数
        verify(jwtUtil, times(1)).generateAccessToken(testUser.getId(), testUser.getStudentId(), testUser.getRole());
        verify(jwtUtil, times(1)).generateRefreshToken(testUser.getId());
    }
    
    @Test
    void testLogin_UserNotFound() {
        // 模拟用户不存在
        when(userMapper.selectOne(any())).thenReturn(null);
        
        // 执行登录并验证异常
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.login(validLoginDTO);
        });
        
        assertEquals("用户不存在", exception.getMessage());
        
        // 验证只调用了查询用户
        verify(userMapper, times(1)).selectOne(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void testLogin_UserBanned() {
        // 设置用户为封禁状态
        testUser.setStatus(1);
        when(userMapper.selectOne(any())).thenReturn(testUser);
        
        // 执行登录并验证异常
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            authService.login(validLoginDTO);
        });
        
        assertEquals("账户已被封禁", exception.getMessage());
        
        // 验证只调用了查询用户
        verify(userMapper, times(1)).selectOne(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void testLogin_AccountLocked() {
        // 模拟查询用户成功
        when(userMapper.selectOne(any())).thenReturn(testUser);
        
        // 模拟Redis中失败次数>=5
        when(redisUtil.get(anyString())).thenReturn("5");
        
        // 执行登录并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(validLoginDTO);
        });
        
        assertEquals("账户已锁定，请15分钟后重试", exception.getMessage());
        
        // 验证只调用了查询用户和Redis
        verify(userMapper, times(1)).selectOne(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void testLogin_WrongPassword() {
        // 模拟查询用户成功
        when(userMapper.selectOne(any())).thenReturn(testUser);
        
        // 模拟Redis中没有失败记录
        when(redisUtil.get(anyString())).thenReturn(null);
        
        // 模拟密码验证失败
        when(passwordEncoder.matches(validLoginDTO.getPassword(), testUser.getPassword())).thenReturn(false);
        
        // 执行登录并验证异常
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.login(validLoginDTO);
        });
        
        assertEquals("密码错误", exception.getMessage());
        
        // 验证失败次数增加
        verify(redisUtil, times(1)).increment(anyString());
        verify(redisUtil, times(1)).expire(anyString(), eq(15L), eq(TimeUnit.MINUTES));
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
    }
    
    @Test
    void testLogin_FirstLoginToday() {
        // 模拟查询用户成功
        when(userMapper.selectOne(any())).thenReturn(testUser);
        
        // 模拟Redis中没有失败记录
        when(redisUtil.get(anyString())).thenReturn(null);
        
        // 模拟密码验证成功
        when(passwordEncoder.matches(validLoginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        
        // 模拟Redis检查首次登录（返回false表示今天首次登录）
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        
        // 模拟JWT生成
        when(jwtUtil.generateAccessToken(testUser.getId(), testUser.getStudentId(), testUser.getRole()))
            .thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(testUser.getId())).thenReturn("refresh-token");
        
        // 执行登录
        TokenVO result = authService.login(validLoginDTO);
        
        // 验证结果
        assertNotNull(result);
        
        // 验证设置了每日登录标记
        verify(redisUtil, times(1)).set(anyString(), eq("1"), eq(1L), eq(TimeUnit.DAYS));
        // TODO: 验证积分增加（当PointService实现后）
    }
    
    @Test
    void testRefreshToken_Success() {
        // 模拟刷新令牌有效
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        
        // 模拟从令牌提取用户ID
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(testUser.getId());
        
        // 模拟查询用户成功
        when(userMapper.selectById(testUser.getId())).thenReturn(testUser);
        
        // 模拟JWT生成
        when(jwtUtil.generateAccessToken(testUser.getId(), testUser.getStudentId(), testUser.getRole()))
            .thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(testUser.getId())).thenReturn("new-refresh-token");
        
        // 执行刷新令牌
        TokenVO result = authService.refreshToken("old-refresh-token");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        assertEquals(7200L, result.getExpiresIn());
        assertNotNull(result.getUserInfo());
        assertEquals(testUser.getStudentId(), result.getUserInfo().getStudentId());
        
        // 验证方法调用
        verify(jwtUtil, times(1)).validateToken(anyString());
        verify(jwtUtil, times(1)).getUserIdFromToken(anyString());
        verify(userMapper, times(1)).selectById(testUser.getId());
        verify(jwtUtil, times(1)).generateAccessToken(testUser.getId(), testUser.getStudentId(), testUser.getRole());
        verify(jwtUtil, times(1)).generateRefreshToken(testUser.getId());
    }
    
    @Test
    void testRefreshToken_InvalidToken() {
        // 模拟刷新令牌无效
        when(jwtUtil.validateToken(anyString())).thenReturn(false);
        
        // 执行刷新令牌并验证异常
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.refreshToken("invalid-refresh-token");
        });
        
        assertEquals("刷新令牌无效或已过期", exception.getMessage());
        
        // 验证只调用了验证方法
        verify(jwtUtil, times(1)).validateToken(anyString());
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
        verify(userMapper, never()).selectById(any());
    }
    
    @Test
    void testRefreshToken_UserNotFound() {
        // 模拟刷新令牌有效
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        
        // 模拟从令牌提取用户ID
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(testUser.getId());
        
        // 模拟用户不存在
        when(userMapper.selectById(testUser.getId())).thenReturn(null);
        
        // 执行刷新令牌并验证异常
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.refreshToken("refresh-token");
        });
        
        assertEquals("用户不存在", exception.getMessage());
        
        // 验证方法调用
        verify(jwtUtil, times(1)).validateToken(anyString());
        verify(jwtUtil, times(1)).getUserIdFromToken(anyString());
        verify(userMapper, times(1)).selectById(testUser.getId());
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
    }
    
    @Test
    void testRefreshToken_UserBanned() {
        // 设置用户为封禁状态
        testUser.setStatus(1);
        
        // 模拟刷新令牌有效
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        
        // 模拟从令牌提取用户ID
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(testUser.getId());
        
        // 模拟查询用户成功
        when(userMapper.selectById(testUser.getId())).thenReturn(testUser);
        
        // 执行刷新令牌并验证异常
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            authService.refreshToken("refresh-token");
        });
        
        assertEquals("账户已被封禁", exception.getMessage());
        
        // 验证方法调用
        verify(jwtUtil, times(1)).validateToken(anyString());
        verify(jwtUtil, times(1)).getUserIdFromToken(anyString());
        verify(userMapper, times(1)).selectById(testUser.getId());
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
    }
}
