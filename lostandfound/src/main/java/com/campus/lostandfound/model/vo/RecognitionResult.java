package com.campus.lostandfound.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 图像识别结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionResult {
    
    /**
     * 物品类别
     */
    private String category;
    
    /**
     * 类别置信度 (0-1)
     */
    private BigDecimal confidence;
    
    /**
     * 识别出的标签列表
     */
    private List<TagInfo> tags;
    
    /**
     * 原始响应（用于调试）
     */
    private String rawResponse;
    
    /**
     * 是否识别成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果识别失败）
     */
    private String errorMessage;
    
    /**
     * 创建空结果（识别失败时使用）
     */
    public static RecognitionResult empty() {
        RecognitionResult result = new RecognitionResult();
        result.setSuccess(false);
        result.setTags(List.of());
        return result;
    }
    
    /**
     * 创建空结果（带错误信息）
     */
    public static RecognitionResult empty(String errorMessage) {
        RecognitionResult result = empty();
        result.setErrorMessage(errorMessage);
        return result;
    }
}
