package com.campus.lostandfound.service.impl;

import com.campus.lostandfound.model.entity.PointRecord;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.repository.PointRecordMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    
    private final PointRecordMapper pointRecordMapper;
    private final UserMapper userMapper;
    
    /**
     * 增加用户积分
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer points, String reason, Long relatedId) {
        log.info("为用户 {} 增加 {} 积分，原因: {}", userId, points, reason);
        
        // 创建积分记录
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setReason(reason);
        record.setRelatedId(relatedId);
        pointRecordMapper.insert(record);
        
        // 更新用户总积分
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + points);
            userMapper.updateById(user);
            log.info("用户 {} 当前总积分: {}", userId, user.getPoints());
        }
    }
}
