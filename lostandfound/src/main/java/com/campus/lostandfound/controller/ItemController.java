package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.dto.ItemSearchDTO;
import com.campus.lostandfound.model.dto.UpdateCategoryDTO;
import com.campus.lostandfound.model.vo.ItemDetailVO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物品信息控制器
 * 提供失物/招领信息的发布、查询、编辑等接口
 */
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {
    
    @Autowired
    private ItemService itemService;
    
    /**
     * 搜索物品列表
     * 支持关键词搜索、多条件筛选、地理范围筛选和多种排序方式
     * 
     * @param dto 搜索条件DTO
     * @return 分页的物品列表
     */
    @GetMapping
    public Result<PageResult<ItemVO>> searchItems(@ModelAttribute ItemSearchDTO dto) {
        // 调用Service搜索物品
        PageResult<ItemVO> result = itemService.search(dto);
        
        return Result.success(result);
    }
    
    /**
     * 获取热门物品列表
     * 返回最近7天浏览量最高的20条记录
     * 
     * @return 热门物品列表
     */
    @GetMapping("/hot")
    public Result<List<ItemVO>> getHotItems() {
        // 调用Service获取热门物品，默认返回20条
        List<ItemVO> items = itemService.getHotItems(20);
        
        return Result.success(items);
    }
    
    /**
     * 发布失物/招领信息
     * 
     * @param dto 物品信息DTO
     * @return 发布后的物品信息
     */
    @PostMapping
    public Result<ItemVO> publishItem(@Valid @RequestBody ItemDTO dto) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        // 调用Service发布物品信息
        ItemVO itemVO = itemService.publish(dto, userId);
        
        return Result.success(itemVO);
    }
    
    /**
     * 更新物品信息
     * 
     * @param id 物品ID
     * @param dto 物品信息DTO
     * @return 更新后的物品信息
     */
    @PutMapping("/{id}")
    public Result<ItemVO> updateItem(@PathVariable Long id, @Valid @RequestBody ItemDTO dto) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        // 调用Service更新物品信息
        ItemVO itemVO = itemService.update(id, dto, userId);
        
        return Result.success(itemVO);
    }
    
    /**
     * 删除物品信息（软删除）
     * 
     * @param id 物品ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        // 调用Service删除物品信息
        itemService.delete(id, userId);
        
        return Result.success(null);
    }
    
    /**
     * 获取物品详情
     * 
     * @param id 物品ID
     * @return 物品详情信息
     */
    @GetMapping("/{id}")
    public Result<ItemDetailVO> getItemDetail(@PathVariable Long id) {
        // 调用Service获取物品详情
        ItemDetailVO itemDetailVO = itemService.getDetail(id);
        
        return Result.success(itemDetailVO);
    }
    
    /**
     * 手动更新物品类别
     * 用于AI识别失败或识别不准确时的降级方案
     * 
     * @param id 物品ID
     * @param dto 类别更新DTO
     * @return 更新后的物品信息
     */
    @PutMapping("/{id}/category")
    public Result<ItemVO> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryDTO dto) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        // 调用Service更新物品类别
        ItemVO itemVO = itemService.updateCategory(id, dto.getCategory(), userId);
        
        return Result.success(itemVO);
    }
    
    /**
     * 获取附近的物品信息
     * 基于用户当前位置返回指定半径范围内的物品列表
     * 
     * @param lng 中心点经度
     * @param lat 中心点纬度
     * @param radius 搜索半径（米），默认1000米
     * @return 附近物品列表，按距离升序排序
     */
    @GetMapping("/nearby")
    public Result<List<ItemVO>> getNearbyItems(
            @RequestParam Double lng,
            @RequestParam Double lat,
            @RequestParam(required = false, defaultValue = "1000") Integer radius) {
        
        // 调用Service获取附近物品
        List<ItemVO> items = itemService.getNearby(lng, lat, radius);
        
        return Result.success(items);
    }
}
