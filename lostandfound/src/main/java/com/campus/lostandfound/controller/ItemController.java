package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ItemDTO;
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
}
