package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ItemAdminSearchDTO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.model.vo.StatisticsVO;
import com.campus.lostandfound.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 管理员控制器
 * 提供平台统计数据和管理功能接口
 * 所有接口仅限管理员访问
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    /**
     * 获取平台统计数据
     * 包括用户总数、信息总数、匹配成功率等核心指标
     * 
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<StatisticsVO> getStatistics() {
        StatisticsVO statistics = adminService.getStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 管理员查看物品列表
     * 支持查询所有物品（包括已删除的），支持按状态、时间、举报次数筛选
     * 
     * @param dto 搜索条件
     * @return 分页物品列表
     */
    @GetMapping("/items")
    public Result<PageResult<ItemVO>> getItemList(@ModelAttribute ItemAdminSearchDTO dto) {
        PageResult<ItemVO> result = adminService.getItemList(dto);
        return Result.success(result);
    }
    
    /**
     * 审核物品信息
     * 管理员对被举报或需要审核的物品进行处理
     * 
     * @param id 物品ID
     * @param action 审核操作: 0-通过, 1-删除, 2-警告发布者
     * @param reason 审核原因/备注
     * @return 操作结果
     */
    @PostMapping("/items/{id}/review")
    public Result<Void> reviewItem(
            @PathVariable("id") Long id,
            @RequestParam("action") Integer action,
            @RequestParam(value = "reason", required = false) String reason) {
        adminService.reviewItem(id, action, reason);
        return Result.success();
    }
    
    /**
     * 封禁用户
     * 更新用户状态为封禁，并向用户发送封禁通知消息
     * 
     * @param id 用户ID
     * @param reason 封禁原因
     * @return 操作结果
     */
    @PostMapping("/users/{id}/ban")
    public Result<Void> banUser(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        adminService.banUser(id, reason);
        return Result.success();
    }
    
    /**
     * 解封用户
     * 更新用户状态为正常，并向用户发送解封通知消息
     * 
     * @param id 用户ID
     * @return 操作结果
     */
    @PostMapping("/users/{id}/unban")
    public Result<Void> unbanUser(@PathVariable("id") Long id) {
        adminService.unbanUser(id);
        return Result.success();
    }
    
    /**
     * 导出数据报表
     * 生成包含指定时间范围内运营数据的Excel文件
     * 
     * @param startDate 开始日期 (格式: yyyy-MM-dd)
     * @param endDate 结束日期 (格式: yyyy-MM-dd)
     * @return Excel文件下载
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        byte[] excelData = adminService.exportReport(startDate, endDate);
        
        // 生成文件名
        String fileName = String.format("运营报表_%s_%s.xlsx",
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(excelData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
