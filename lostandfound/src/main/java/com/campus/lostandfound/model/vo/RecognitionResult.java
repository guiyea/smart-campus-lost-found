package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 图像识别结果
 */
@Schema(description = "图像识别结果")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionResult {
    
    /**
     * 物品类别
     */
    @Schema(description = "物品类别", example = "电子设备", accessMode = Schema.AccessMode.READ_ONLY)
    private String category;
    
    /**
     * 类别置信度 (0-1)
     */
    @Schema(description = "类别置信度 (0-1)", example = "0.85", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal confidence;
    
    /**
     * 识别出的标签列表
     */
    @Schema(description = "识别出的标签列表", accessMode = Schema.AccessMode.READ_ONLY)
    private List<TagInfo> tags;
    
    /**
     * 原始响应（用于调试）
     */
    @Schema(description = "原始响应（用于调试）", accessMode = Schema.AccessMode.READ_ONLY)
    private String rawResponse;
    
    /**
     * 是否识别成功
     */
    @Schema(description = "是否识别成功", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean success;
    
    /**
     * 错误信息（如果识别失败）
     */
    @Schema(description = "错误信息（如果识别失败）", example = "网络超时", accessMode = Schema.AccessMode.READ_ONLY)
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
