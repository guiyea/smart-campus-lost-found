package com.campus.lostandfound.service;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.dto.ItemSearchDTO;
import com.campus.lostandfound.model.entity.Item;
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
    
    /**
     * 手动更新物品类别
     * 用于AI识别失败或识别不准确时的降级方案
     * 
     * @param id 物品ID
     * @param category 物品类别
     * @param userId 当前用户ID
     * @return 更新后的物品信息VO
     */
    ItemVO updateCategory(Long id, String category, Long userId);
    
    /**
     * 获取附近的物品信息
     * 基于用户当前位置返回指定半径范围内的物品列表
     * 
     * @param lng 中心点经度
     * @param lat 中心点纬度
     * @param radius 搜索半径（米），默认1000米
     * @return 附近物品列表，按距离升序排序
     */
    java.util.List<ItemVO> getNearby(Double lng, Double lat, Integer radius);
    
    /**
     * 搜索物品列表
     * 支持关键词搜索、多条件筛选、地理范围筛选和多种排序方式
     * 
     * @param dto 搜索条件DTO
     * @return 分页的物品列表
     */
    PageResult<ItemVO> search(ItemSearchDTO dto);
    
    /**
     * 获取热门物品列表
     * 返回最近7天浏览量最高的物品
     * 
     * @param limit 返回数量限制，默认20
     * @return 热门物品列表，按浏览量降序排序
     */
    java.util.List<ItemVO> getHotItems(Integer limit);
    
    /**
     * 将Item实体转换为ItemVO
     * 
     * @param item 物品实体
     * @return 物品VO
     */
    ItemVO convertToVO(Item item);
}
