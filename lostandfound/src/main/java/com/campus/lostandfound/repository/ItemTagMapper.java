package com.campus.lostandfound.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostandfound.model.entity.ItemTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品标签Mapper接口
 */
@Mapper
public interface ItemTagMapper extends BaseMapper<ItemTag> {
}
