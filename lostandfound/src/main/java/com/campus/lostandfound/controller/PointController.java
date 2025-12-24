package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.vo.PointRankVO;
import com.campus.lostandfound.model.vo.PointRecordVO;
import com.campus.lostandfound.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分控制器
 * 提供积分明细查询、总积分查询、积分排行榜等接口
 */
@RestController
@RequestMapping("/api/v1/points")
public class PointController {
    
    @Autowired
    private PointService pointService;
    
    /**
     * 获取当前用户积分明细
     * 
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认20
     * @return 分页的积分记录列表
     */
    @GetMapping
    public Result<PageResult<PointRecordVO>> getPointRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        PageResult<PointRecordVO> pageResult = pointService.getRecords(userId, pageNum, pageSize);
        return Result.success(pageResult);
    }
    
    /**
     * 获取当前用户总积分
     * 
     * @return 用户总积分
     */
    @GetMapping("/total")
    public Result<Integer> getTotalPoints() {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        
        Integer totalPoints = pointService.getTotalPoints(userId);
        return Result.success(totalPoints);
    }
    
    /**
     * 获取积分排行榜
     * 
     * @param limit 返回数量限制，默认100，最大100
     * @return 积分排行榜列表
     */
    @GetMapping("/ranking")
    public Result<List<PointRankVO>> getRanking(
            @RequestParam(defaultValue = "100") Integer limit) {
        // 限制最大返回数量为100
        if (limit > 100) {
            limit = 100;
        }
        
        List<PointRankVO> ranking = pointService.getRanking(limit);
        return Result.success(ranking);
    }
}
