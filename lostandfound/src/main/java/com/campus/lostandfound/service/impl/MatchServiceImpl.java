package com.campus.lostandfound.service.impl;

import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 匹配服务实现类（占位实现）
 * 完整实现将在后续任务中完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {
    
    @Override
    public void calculateMatchAsync(Item item) {
        log.info("匹配服务占位实现 - itemId: {}, type: {}", item.getId(), item.getType());
        // TODO: 将在任务18.2中实现完整的匹配计算功能
    }
    
    @Override
    public List<ItemVO> getRecommendations(Long itemId) {
        log.info("获取匹配推荐占位实现 - itemId: {}", itemId);
        // TODO: 将在任务18.4中实现完整的匹配推荐功能
        // 目前返回空列表
        return new ArrayList<>();
    }
}
