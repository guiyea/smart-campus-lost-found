package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.vo.ItemDetailVO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}
