package com.campus.lostandfound.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostandfound.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
