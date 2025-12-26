package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 图像识别标签信息
 */
@Schema(description = "图像识别标签信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagInfo {
    
    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "黑色", accessMode = Schema.AccessMode.READ_ONLY)
    private String tag;
    
    /**
     * 置信度 (0-1)
     */
    @Schema(description = "置信度 (0-1)", example = "0.92", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal confidence;
}
