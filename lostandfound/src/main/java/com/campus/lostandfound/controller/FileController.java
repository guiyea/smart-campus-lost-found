package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 上传单个文件
     * Requirements: 2.1, 10.2
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("上传文件，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String url = fileService.uploadImage(file);
        return Result.success(url);
    }
    
    /**
     * 上传物品图片
     */
    @PostMapping("/upload/item")
    public Result<String> uploadItemImage(@RequestParam("file") MultipartFile file) {
        log.info("上传物品图片，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String url = fileService.uploadItemImage(file);
        return Result.success(url);
    }
    
    /**
     * 批量上传文件（最多9张）
     * Requirements: 2.1, 10.2
     */
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        log.info("批量上传文件，数量: {}", files.length);
        
        // 验证文件数量不超过9张
        if (files.length > 9) {
            return Result.error(400, "批量上传最多支持9张图片");
        }
        
        List<String> urls = fileService.uploadItemImages(Arrays.asList(files));
        return Result.success(urls);
    }
    
    /**
     * 批量上传物品图片
     */
    @PostMapping("/upload/items")
    public Result<List<String>> uploadItemImages(@RequestParam("files") MultipartFile[] files) {
        log.info("批量上传物品图片，数量: {}", files.length);
        List<String> urls = fileService.uploadItemImages(Arrays.asList(files));
        return Result.success(urls);
    }
    
    /**
     * 上传用户头像
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传用户头像，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String url = fileService.uploadAvatar(file);
        return Result.success(url);
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam("url") String fileUrl) {
        log.info("删除文件: {}", fileUrl);
        fileService.deleteFile(fileUrl);
        return Result.success();
    }
}
