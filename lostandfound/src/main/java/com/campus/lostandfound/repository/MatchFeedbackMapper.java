package com.campus.lostandfound.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostandfound.model.entity.MatchFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 匹配反馈Mapper接口
 */
@Mapper
public interface MatchFeedbackMapper extends BaseMapper<MatchFeedback> {
    
}