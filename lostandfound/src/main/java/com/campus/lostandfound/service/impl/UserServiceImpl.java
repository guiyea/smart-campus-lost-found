package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.exception.BusinessException;
import com.campus.lostandfound.exception.NotFoundException;
import com.campus.lostandfound.model.dto.UpdateProfileDTO;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.UserVO;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.CacheService;
import com.campus.lostandfound.service.UserService;
import com.campus.lostandfound.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    // 缓存键前缀
    private static final String USER_INFO_CACHE_KEY = "user:info:";
    
    // 缓存过期时间（1小时）
    private static final long USER_INFO_CACHE_HOURS = 1;
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    
    /**
     * 获取用户信息
     * 使用Redis缓存，过期时间1小时
     * 
     * @param userId 用户ID
     * @return 用户信息VO
     */
    @Override
    public UserVO getProfile(Long userId) {
        String cacheKey = USER_INFO_CACHE_KEY + userId;
        
        // 1. 尝试从缓存获取
        String cachedData = redisUtil.get(cacheKey);
        if (cachedData != null) {
            try {
                UserVO cachedVO = objectMapper.readValue(cachedData, UserVO.class);
                log.info("用户信息缓存命中: userId={}", userId);
                return cachedVO;
            } catch (JsonProcessingException e) {
                log.warn("解析用户信息缓存失败: userId={}", userId, e);
            }
        }
        
        // 2. 缓存未命中，从数据库查询
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        
        UserVO userVO = convertToVO(user);
        
        // 3. 存入缓存
        try {
            String jsonData = objectMapper.writeValueAsString(userVO);
            redisUtil.set(cacheKey, jsonData, USER_INFO_CACHE_HOURS, TimeUnit.HOURS);
            log.info("用户信息已缓存: userId={}", userId);
        } catch (JsonProcessingException e) {
            log.warn("缓存用户信息失败: userId={}", userId, e);
        }
        
        return userVO;
    }
    
    /**
     * 更新用户信息
     * 更新后清除缓存
     * 
     * @param userId 用户ID
     * @param dto 更新信息DTO
     * @return 更新后的用户信息VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        
        // 如果更新手机号，验证新手机号是否已被其他用户使用
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(User::getPhone, dto.getPhone())
                      .ne(User::getId, userId);
            Long count = userMapper.selectCount(phoneQuery);
            if (count > 0) {
                throw new BusinessException("该手机号已被其他用户使用");
            }
            user.setPhone(dto.getPhone());
        }
        
        // 如果更新密码，验证旧密码是否正确
        if (StringUtils.hasText(dto.getNewPassword())) {
            if (!StringUtils.hasText(dto.getOldPassword())) {
                throw new BusinessException("请输入旧密码");
            }
            
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new BusinessException("旧密码不正确");
            }
            
            // 加密新密码
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }
        
        // 更新其他非空字段
        if (StringUtils.hasText(dto.getName())) {
            user.setName(dto.getName());
        }
        
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }
        
        // 保存更新
        userMapper.updateById(user);
        
        // 清除用户信息缓存
        invalidateUserCache(userId);
        
        // 返回更新后的用户信息
        return convertToVO(user);
    }
    
    /**
     * 清除用户信息缓存
     * 使用CacheService统一管理缓存清除
     */
    private void invalidateUserCache(Long userId) {
        // 使用CacheService清除用户信息缓存
        cacheService.evictUserInfoCache(userId);
    }
    
    /**
     * 获取用户列表（管理员功能）
     * 
     * @param studentId 学号（可选，支持模糊查询）
     * @param name 姓名（可选，支持模糊查询）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页用户列表
     */
    @Override
    public PageResult<UserVO> getUserList(String studentId, String name, Integer pageNum, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 学号模糊查询
        if (StringUtils.hasText(studentId)) {
            queryWrapper.like(User::getStudentId, studentId);
        }
        
        // 姓名模糊查询
        if (StringUtils.hasText(name)) {
            queryWrapper.like(User::getName, name);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(User::getCreatedAt);
        
        // 分页查询
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换为VO列表
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 返回分页结果
        return new PageResult<>(
                userVOList,
                userPage.getTotal(),
                pageNum,
                pageSize
        );
    }
    
    /**
     * 将User实体转换为UserVO
     * 
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 脱敏手机号
        userVO.maskPhone();
        return userVO;
    }
}
