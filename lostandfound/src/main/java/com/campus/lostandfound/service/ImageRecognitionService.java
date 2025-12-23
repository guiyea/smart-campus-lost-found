package com.campus.lostandfound.service;

import com.campus.lostandfound.model.vo.RecognitionResult;

import java.util.List;

/**
 * 图像识别服务接口
 */
public interface ImageRecognitionService {
    
    /**
     * 识别图片内容
     * 
     * @param imageUrl 图片URL（OSS URL）
     * @return 识别结果
     */
    RecognitionResult recognize(String imageUrl);
    
    /**
     * 提取物品特征标签
     * 
     * @param imageUrl 图片URL
     * @return 标签列表
     */
    List<String> extractTags(String imageUrl);
    
    /**
     * 获取物品类别
     * 根据识别结果映射到系统预定义类别
     * 
     * @param imageUrl 图片URL
     * @return 物品类别
     */
    String getCategory(String imageUrl);
    
    /**
     * 识别图片并保存标签到数据库
     * 
     * @param imageUrl 图片URL
     * @param itemId 物品ID
     */
    void recognizeAndSaveTags(String imageUrl, Long itemId);
}
