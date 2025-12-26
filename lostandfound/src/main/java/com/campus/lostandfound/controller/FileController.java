package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理", description = "文件上传、删除等接口")
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
    @Operation(summary = "上传单个文件", description = "上传单个图片文件，支持JPG、PNG格式，最大10MB", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "文件格式不支持或文件过大"),
        @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PostMapping("/upload")
    public Result<String> upload(
            @Parameter(description = "上传的图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("上传文件，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String url = fileService.uploadImage(file);
        return Result.success(url);
    }
    
    /**
     * 上传物品图片
     */
    @Operation(summary = "上传物品图片", description = "上传物品图片，支持JPG、PNG格式，最大10MB", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/upload/item")
    public Result<String> uploadItemImage(
            @Parameter(description = "上传的物品图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("上传物品图片，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String url = fileService.uploadItemImage(file);
        return Result.success(url);
    }
    
    /**
     * 批量上传文件（最多9张）
     * Requirements: 2.1, 10.2
     */
    @Operation(summary = "批量上传文件", description = "批量上传文件，最多支持9张图片，支持JPG、PNG格式，每张最大10MB", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadBatch(
            @Parameter(description = "上传的图片文件数组，最多9张", required = true)
            @RequestParam("files") MultipartFile[] files) {
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
    @Operation(summary = "批量上传物品图片", description = "批量上传物品图片，最多支持9张，支持JPG、PNG格式，每张最大10MB", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/upload/items")
    public Result<List<String>> uploadItemImages(
            @Parameter(description = "上传的物品图片文件数组，最多9张", required = true)
            @RequestParam("files") MultipartFile[] files) {
        log.info("批量上传物品图片，数量: {}", files.length);
        List<String> urls = fileService.uploadItemImages(Arrays.asList(files));
        return Result.success(urls);
    }
    
    /**
     * 上传用户头像
     */
    @Operation(summary = "上传用户头像", description = "上传用户头像，支持JPG、PNG格式，最大10MB", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(
            @Parameter(description = "上传的头像图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("上传用户头像，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String url = fileService.uploadAvatar(file);
        return Result.success(url);
    }
    
    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "根据文件URL删除文件", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping
    public Result<Void> deleteFile(
            @Parameter(description = "要删除的文件URL", required = true, example = "https://example.com/image.jpg")
            @RequestParam("url") String fileUrl) {
        log.info("删除文件: {}", fileUrl);
        fileService.deleteFile(fileUrl);
        return Result.success();
    }
}
