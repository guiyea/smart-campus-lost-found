package com.campus.lostandfound.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostandfound.model.entity.MatchRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 匹配记录Mapper接口
 */
@Mapper
public interface MatchRecordMapper extends BaseMapper<MatchRecord> {
}
