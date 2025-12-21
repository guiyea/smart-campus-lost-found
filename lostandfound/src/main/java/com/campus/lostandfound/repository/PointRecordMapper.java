package com.campus.lostandfound.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostandfound.model.entity.PointRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分记录Mapper接口
 */
@Mapper
public interface PointRecordMapper extends BaseMapper<PointRecord> {
}
