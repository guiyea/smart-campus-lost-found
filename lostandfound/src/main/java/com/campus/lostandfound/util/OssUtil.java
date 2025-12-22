package com.campus.lostandfound.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.campus.lostandfound.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {
    
    private final OSS ossClient;
    private final OssProperties ossProperties;
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @param folder 文件夹路径（如：images、avatars）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 生成文件名：folder/yyyy/MM/dd/uuid.ext
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = String.format("%s/%s/%s%s", 
                folder, 
                dateFolder, 
                UUID.randomUUID().toString().replace("-", ""),
                extension);
        
        // 设置元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        
        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    fileName,
                    inputStream,
                    metadata
            );
            
            ossClient.putObject(putObjectRequest);
            
            // 返回文件URL
            String fileUrl = ossProperties.getBaseUrl() + "/" + fileName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败: {}", fileName, e);
            throw new IOException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String fileName = fileUrl.replace(ossProperties.getBaseUrl() + "/", "");
            ossClient.deleteObject(ossProperties.getBucketName(), fileName);
            log.info("文件删除成功: {}", fileName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查文件是否存在
     *
     * @param fileUrl 文件URL
     * @return 是否存在
     */
    public boolean fileExists(String fileUrl) {
        try {
            String fileName = fileUrl.replace(ossProperties.getBaseUrl() + "/", "");
            return ossClient.doesObjectExist(ossProperties.getBucketName(), fileName);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", fileUrl, e);
            return false;
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
