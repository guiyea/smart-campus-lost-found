package com.campus.lostandfound.service;

import com.campus.lostandfound.exception.BusinessException;
import com.campus.lostandfound.model.dto.RegisterDTO;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.impl.AuthServiceImpl;
import com.campus.lostandfound.util.JwtUtil;
import com.campus.lostandfound.util.RedisUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户注册属性测试
 * 
 * Feature: smart-campus-lost-found, Property 1: 用户注册数据完整性
 * Validates: Requirements 1.1
 */
class AuthServicePropertyTest {

    /**
     * Property 1: 用户注册数据完整性 - 注册后查询用户应返回相同的studentId, name, phone
     * 
     * Feature: smart-campus-lost-found, Property 1: 用户注册数据完整性
     * 测试: 对于任意有效注册信息，注册后查询用户应返回相同的studentId, name, phone
     * Validates: Requirements 1.1
     */
    @Property(tries = 100)
    void registrationShouldPreserveUserData(
            @ForAll("validStudentIds") String studentId,
            @ForAll("validNames") String name,
            @ForAll("validPhones") String phone,
            @ForAll("validPasswords") String password) {
        
        // Setup mocks
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        AuthServiceImpl authService = new AuthServiceImpl(userMapper, passwordEncoder, jwtUtil, redisUtil);
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 7200000L);
        
        // Mock: studentId and phone don't exist
        when(userMapper.selectCount(any())).thenReturn(0L);
        
        // Capture the user being inserted
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userMapper.insert(userCaptor.capture())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        
        // Create registration DTO
        RegisterDTO dto = new RegisterDTO();
        dto.setStudentId(studentId);
        dto.setName(name);
        dto.setPhone(phone);
        dto.setPassword(password);
        
        // Execute registration
        UserVO result = authService.register(dto);
        
        // Verify the captured user has the same data
        User capturedUser = userCaptor.getValue();
        assertEquals(studentId, capturedUser.getStudentId(), 
            "Registered user should have the same studentId");
        assertEquals(name, capturedUser.getName(), 
            "Registered user should have the same name");
        assertEquals(phone, capturedUser.getPhone(), 
            "Registered user should have the same phone");
        
        // Verify the returned UserVO has the same data (phone is masked)
        assertEquals(studentId, result.getStudentId(), 
            "Returned UserVO should have the same studentId");
        assertEquals(name, result.getName(), 
            "Returned UserVO should have the same name");
        
        // Verify phone is masked correctly (format: 138****8000)
        String expectedMaskedPhone = phone.substring(0, 3) + "****" + phone.substring(7);
        assertEquals(expectedMaskedPhone, result.getPhone(), 
            "Returned UserVO should have masked phone");
    }

    /**
     * Property 1: 用户注册数据完整性 - 重复学号应抛出异常
     * 
     * Feature: smart-campus-lost-found, Property 1: 用户注册数据完整性
     * 测试: 对于任意重复学号，注册应抛出异常
     * Validates: Requirements 1.1
     */
    @Property(tries = 100)
    void duplicateStudentIdShouldThrowException(
            @ForAll("validStudentIds") String studentId,
            @ForAll("validNames") String name,
            @ForAll("validPhones") String phone,
            @ForAll("validPasswords") String password) {
        
        // Setup mocks
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        AuthServiceImpl authService = new AuthServiceImpl(userMapper, passwordEncoder, jwtUtil, redisUtil);
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 7200000L);
        
        // Mock: studentId already exists
        when(userMapper.selectCount(any())).thenReturn(1L);
        
        // Create registration DTO
        RegisterDTO dto = new RegisterDTO();
        dto.setStudentId(studentId);
        dto.setName(name);
        dto.setPhone(phone);
        dto.setPassword(password);
        
        // Execute registration and verify exception
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(dto);
        });
        
        assertEquals("学号已存在", exception.getMessage(), 
            "Should throw exception with correct message for duplicate studentId");
        
        // Verify insert was never called
        verify(userMapper, never()).insert(any(User.class));
    }

    // ==================== Property 20: 密码加密存储 ====================

    /**
     * Property 20: 密码加密存储 - BCrypt格式验证
     * 
     * Feature: smart-campus-lost-found, Property 20: 密码加密存储
     * 测试: 对于任意密码，存储后的值应以$2a$或$2b$开头（BCrypt格式）
     * Validates: Requirements 10.4
     */
    @Property(tries = 100)
    void encryptedPasswordShouldHaveBCryptFormat(
            @ForAll("validPasswords") String rawPassword) {
        
        // Use BCryptPasswordEncoder (same as in production)
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // Encrypt the password
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Verify BCrypt format: should start with $2a$ or $2b$
        assertTrue(
            encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"),
            "Encoded password should start with $2a$ or $2b$ (BCrypt format), but was: " + encodedPassword
        );
    }

    /**
     * Property 20: 密码加密存储 - 加密后与原密码不相等
     * 
     * Feature: smart-campus-lost-found, Property 20: 密码加密存储
     * 测试: 对于任意密码，存储值与原始密码不相等
     * Validates: Requirements 10.4
     */
    @Property(tries = 100)
    void encryptedPasswordShouldNotEqualRawPassword(
            @ForAll("validPasswords") String rawPassword) {
        
        // Use BCryptPasswordEncoder (same as in production)
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // Encrypt the password
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Verify encoded password is different from raw password
        assertNotEquals(rawPassword, encodedPassword,
            "Encoded password should not equal raw password");
    }

    /**
     * Property 20: 密码加密存储 - BCrypt验证应返回true
     * 
     * Feature: smart-campus-lost-found, Property 20: 密码加密存储
     * 测试: 对于任意密码，使用BCrypt.matches验证应返回true
     * Validates: Requirements 10.4
     */
    @Property(tries = 100)
    void bcryptMatchesShouldReturnTrueForCorrectPassword(
            @ForAll("validPasswords") String rawPassword) {
        
        // Use BCryptPasswordEncoder (same as in production)
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // Encrypt the password
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Verify BCrypt.matches returns true for correct password
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
            "BCrypt.matches should return true for correct password");
    }

    // ==================== Generators ====================

    /**
     * Generator for valid student IDs (5-20 characters)
     */
    @Provide
    Arbitrary<String> validStudentIds() {
        return Arbitraries.strings()
                .numeric()
                .ofMinLength(5)
                .ofMaxLength(20);
    }

    /**
     * Generator for valid names (2-20 characters, Chinese or English)
     */
    @Provide
    Arbitrary<String> validNames() {
        // Generate Chinese names or English names
        Arbitrary<String> chineseNames = Arbitraries.strings()
                .withCharRange('\u4e00', '\u9fa5') // Chinese characters
                .ofMinLength(2)
                .ofMaxLength(10);
        
        Arbitrary<String> englishNames = Arbitraries.strings()
                .alpha()
                .ofMinLength(2)
                .ofMaxLength(20);
        
        return Arbitraries.oneOf(chineseNames, englishNames);
    }

    /**
     * Generator for valid phone numbers (Chinese mobile format: 1[3-9]xxxxxxxxx)
     */
    @Provide
    Arbitrary<String> validPhones() {
        return Arbitraries.integers().between(3, 9)
                .flatMap(secondDigit -> 
                    Arbitraries.strings()
                            .numeric()
                            .ofLength(9)
                            .map(suffix -> "1" + secondDigit + suffix)
                );
    }

    /**
     * Generator for valid passwords (6-20 characters)
     */
    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(6)
                .ofMaxLength(20);
    }
}
