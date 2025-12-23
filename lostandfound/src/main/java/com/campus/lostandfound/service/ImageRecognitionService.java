package com.campus.lostandfound.service;

import java.util.List;

/**
 * 图像识别服务接口
 */
public interface ImageRecognitionService {
    
    /**
     * 识别图片并提取标签
     * 
     * @param imageUrl 图片URL
     * @param itemId 物品ID
     */
    void recognizeAndSaveTags(String imageUrl, Long itemId);
}
