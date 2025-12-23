package com.campus.lostandfound.service.impl;

import com.campus.lostandfound.service.ImageRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 图像识别服务实现类（占位实现）
 * 完整实现将在后续任务中完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageRecognitionServiceImpl implements ImageRecognitionService {
    
    @Override
    public void recognizeAndSaveTags(String imageUrl, Long itemId) {
        log.info("图像识别服务占位实现 - itemId: {}, imageUrl: {}", itemId, imageUrl);
        // TODO: 将在任务14.2中实现完整的图像识别功能
    }
}
