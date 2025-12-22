package com.campus.lostandfound.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传图片
     * - 验证文件类型（仅允许jpg, jpeg, png, gif, webp）
     * - 验证文件大小（不超过10MB）
     * - 如果文件大于5MB，调用压缩方法
     * - 生成唯一文件名: images/{yyyy/MM/dd}/{uuid}.{ext}
     * - 上传到OSS
     * - 返回文件访问URL
     *
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(MultipartFile file);
    
    /**
     * 压缩图片到5MB以下
     * 使用Thumbnailator压缩图片
     *
     * @param file 原始图片文件
     * @return 压缩后的图片文件
     */
    MultipartFile compressImage(MultipartFile file);
    
    /**
     * 从OSS删除文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 上传物品图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadItemImage(MultipartFile file);
    
    /**
     * 批量上传物品图片
     *
     * @param files 图片文件列表
     * @return 图片URL列表
     */
    List<String> uploadItemImages(List<MultipartFile> files);
    
    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(MultipartFile file);
}
