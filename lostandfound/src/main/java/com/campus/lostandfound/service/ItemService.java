package com.campus.lostandfound.service;

import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.vo.ItemDetailVO;
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
    
    /**
     * 删除物品信息（软删除）
     * 
     * @param id 物品ID
     * @param userId 当前用户ID
     */
    void delete(Long id, Long userId);
    
    /**
     * 获取物品详情
     * 
     * @param id 物品ID
     * @return 物品详情VO
     */
    ItemDetailVO getDetail(Long id);
}
