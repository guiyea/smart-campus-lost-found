package com.campus.lostandfound.service;

import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.vo.ItemVO;

/**
 * 物品信息服务接口
 */
public interface ItemService {
    
    /**
     * 发布失物/招领信息
     * 
     * @param dto 物品信息DTO
     * @param userId 发布用户ID
     * @return 物品信息VO
     */
    ItemVO publish(ItemDTO dto, Long userId);
    
    /**
     * 更新物品信息
     * 
     * @param id 物品ID
     * @param dto 物品信息DTO
     * @param userId 当前用户ID
     * @return 更新后的物品信息VO
     */
    ItemVO update(Long id, ItemDTO dto, Long userId);
}
