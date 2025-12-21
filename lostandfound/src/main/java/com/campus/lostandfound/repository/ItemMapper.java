package com.campus.lostandfound.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostandfound.model.entity.Item;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品信息Mapper接口
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}
