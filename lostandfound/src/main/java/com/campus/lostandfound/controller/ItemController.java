package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.dto.ItemSearchDTO;
import com.campus.lostandfound.model.dto.UpdateCategoryDTO;
import com.campus.lostandfound.model.vo.ItemDetailVO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "物品管理", description = "失物/招领信息的发布、查询、编辑等接口")
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
    @Operation(summary = "搜索物品列表", description = "支持关键词搜索、多条件筛选、地理范围筛选和多种排序方式")
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
    @Operation(summary = "获取热门物品列表", description = "返回最近7天浏览量最高的20条记录")
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
    @Operation(summary = "发布失物/招领信息", description = "发布新的失物或招领信息，需要上传至少一张图片", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "发布成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证")
    })
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
    @Operation(summary = "更新物品信息", description = "更新已发布的物品信息，只能更新自己发布的物品", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public Result<ItemVO> updateItem(
            @Parameter(description = "物品ID", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody ItemDTO dto) {
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
    @Operation(summary = "删除物品信息", description = "软删除物品信息，只能删除自己发布的物品", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(
            @Parameter(description = "物品ID", required = true, example = "1")
            @PathVariable Long id) {
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
    @Operation(summary = "获取物品详情", description = "获取物品的详细信息，包括发布者信息、图片、位置等")
    @GetMapping("/{id}")
    public Result<ItemDetailVO> getItemDetail(
            @Parameter(description = "物品ID", required = true, example = "1")
            @PathVariable Long id) {
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
    @Operation(summary = "更新物品类别", description = "手动更新物品类别，用于AI识别失败或识别不准确时的降级方案", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/category")
    public Result<ItemVO> updateCategory(
            @Parameter(description = "物品ID", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody UpdateCategoryDTO dto) {
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
    @Operation(summary = "获取附近的物品", description = "基于用户当前位置返回指定半径范围内的物品列表，按距离升序排序")
    @GetMapping("/nearby")
    public Result<List<ItemVO>> getNearbyItems(
            @Parameter(description = "中心点经度", required = true, example = "121.4737")
            @RequestParam Double lng,
            @Parameter(description = "中心点纬度", required = true, example = "31.2304")
            @RequestParam Double lat,
            @Parameter(description = "搜索半径（米）", example = "1000")
            @RequestParam(required = false, defaultValue = "1000") Integer radius) {
        
        // 调用Service获取附近物品
        List<ItemVO> items = itemService.getNearby(lng, lat, radius);
        
        return Result.success(items);
    }
}
