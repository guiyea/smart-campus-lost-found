package com.campus.lostandfound.service.impl;

import com.campus.lostandfound.exception.ValidationException;
import com.campus.lostandfound.service.FileService;
import com.campus.lostandfound.util.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final OssUtil ossUtil;
    
    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 压缩阈值：5MB
    private static final long COMPRESS_THRESHOLD = 5 * 1024 * 1024;
    
    @Override
    public String uploadImage(MultipartFile file) {
        validateImageFile(file);
        
        try {
            // 如果文件大于5MB，先压缩
            MultipartFile fileToUpload = file;
            if (file.getSize() > COMPRESS_THRESHOLD) {
                log.info("文件大小 {} 超过5MB，开始压缩", file.getSize());
                fileToUpload = compressImage(file);
                log.info("压缩后文件大小: {}", fileToUpload.getSize());
            }
            
            // 上传到OSS，使用images文件夹
            return ossUtil.uploadFile(fileToUpload, "images");
        } catch (IOException e) {
            log.error("上传图片失败", e);
            throw new RuntimeException("上传图片失败: " + e.getMessage());
        }
    }
    
    @Override
    public MultipartFile compressImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("文件不能为空");
        }
        
        try {
            // 获取原始文件信息
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            // 计算压缩质量，目标是压缩到5MB以下
            double quality = calculateCompressionQuality(file.getSize());
            
            // 使用Thumbnailator压缩图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .scale(1.0) // 保持原始尺寸
                    .outputQuality(quality) // 设置压缩质量
                    .toOutputStream(outputStream);
            
            byte[] compressedBytes = outputStream.toByteArray();
            
            // 如果压缩后仍然大于5MB，进一步降低质量
            int attempts = 0;
            while (compressedBytes.length > COMPRESS_THRESHOLD && attempts < 3) {
                attempts++;
                quality = quality * 0.7; // 每次降低30%质量
                log.info("第{}次压缩，质量调整为: {}", attempts, quality);
                
                outputStream = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                        .scale(1.0)
                        .outputQuality(quality)
                        .toOutputStream(outputStream);
                
                compressedBytes = outputStream.toByteArray();
            }
            
            // 如果还是太大，尝试缩小尺寸
            if (compressedBytes.length > COMPRESS_THRESHOLD) {
                log.info("质量压缩不够，尝试缩小尺寸");
                outputStream = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                        .scale(0.8) // 缩小到80%
                        .outputQuality(0.8)
                        .toOutputStream(outputStream);
                
                compressedBytes = outputStream.toByteArray();
            }
            
            log.info("图片压缩完成，原始大小: {}, 压缩后大小: {}", 
                    file.getSize(), compressedBytes.length);
            
            // 创建新的MultipartFile
            return new CompressedMultipartFile(
                    file.getName(),
                    originalFilename,
                    contentType,
                    compressedBytes
            );
            
        } catch (IOException e) {
            log.error("压缩图片失败", e);
            throw new RuntimeException("压缩图片失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new ValidationException("文件URL不能为空");
        }
        ossUtil.deleteFile(fileUrl);
    }
    
    @Override
    public String uploadItemImage(MultipartFile file) {
        // 复用uploadImage方法
        return uploadImage(file);
    }
    
    @Override
    public List<String> uploadItemImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("请选择要上传的图片");
        }
        
        if (files.size() > 9) {
            throw new ValidationException("最多只能上传9张图片");
        }
        
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadImage(file);
            urls.add(url);
        }
        return urls;
    }
    
    @Override
    public String uploadAvatar(MultipartFile file) {
        validateImageFile(file);
        
        try {
            // 头像也可能需要压缩
            MultipartFile fileToUpload = file;
            if (file.getSize() > COMPRESS_THRESHOLD) {
                fileToUpload = compressImage(file);
            }
            
            return ossUtil.uploadFile(fileToUpload, "avatars");
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw new RuntimeException("上传头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("请选择要上传的图片");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("图片大小不能超过10MB");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("只支持上传 JPG、PNG、GIF、WEBP 格式的图片");
        }
    }
    
    /**
     * 计算压缩质量
     * 根据文件大小动态计算合适的压缩质量
     */
    private double calculateCompressionQuality(long fileSize) {
        // 5MB -> 0.9
        // 6MB -> 0.8
        // 7MB -> 0.7
        // 8MB -> 0.6
        // 9MB -> 0.5
        // 10MB -> 0.5
        
        if (fileSize <= COMPRESS_THRESHOLD) {
            return 1.0;
        }
        
        double ratio = (double) COMPRESS_THRESHOLD / fileSize;
        double quality = Math.max(0.5, ratio * 0.9);
        
        log.info("文件大小: {}, 计算压缩质量: {}", fileSize, quality);
        return quality;
    }
    
    /**
     * 压缩后的MultipartFile实现
     */
    private static class CompressedMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;
        
        public CompressedMultipartFile(String name, String originalFilename, 
                                      String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }
        
        @Override
        public long getSize() {
            return content.length;
        }
        
        @Override
        public byte[] getBytes() {
            return content;
        }
        
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }
        
        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }
}
