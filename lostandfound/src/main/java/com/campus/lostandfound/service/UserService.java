package com.campus.lostandfound.service;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.dto.UpdateProfileDTO;
import com.campus.lostandfound.model.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserVO getProfile(Long userId);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param dto 更新信息DTO
     * @return 更新后的用户信息VO
     */
    UserVO updateProfile(Long userId, UpdateProfileDTO dto);
    
    /**
     * 获取用户列表（管理员功能）
     * 
     * @param studentId 学号（可选，支持模糊查询）
     * @param name 姓名（可选，支持模糊查询）
     * @param status 状态（可选，0-正常，1-封禁）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页用户列表
     */
    PageResult<UserVO> getUserList(String studentId, String name, Integer status, Integer pageNum, Integer pageSize);
}
