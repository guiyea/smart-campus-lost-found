package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品信息发布DTO
 */
@Schema(description = "物品信息发布")
@Data
public class ItemDTO {
    
    /**
     * 标题
     */
    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "丢失一部iPhone 14", minLength = 2, maxLength = 50)
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 50, message = "标题长度必须在2-50个字符之间")
    private String title;
    
    /**
     * 描述
     */
    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "在图书馆三楼丢失一部iPhone 14，黑色，有保护壳", minLength = 10, maxLength = 1000)
    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 1000, message = "描述长度必须在10-1000个字符之间")
    private String description;
    
    /**
     * 类型：0失物 1招领
     */
    @Schema(description = "类型：0-失物, 1-招领", requiredMode = Schema.RequiredMode.REQUIRED, example = "0", allowableValues = {"0", "1"})
    @NotNull(message = "类型不能为空")
    private Integer type;
    
    /**
     * 物品类别（可由AI识别自动填充）
     */
    @Schema(description = "物品类别", example = "电子设备")
    private String category;
    
    /**
     * 图片URL列表
     */
    @ArraySchema(
        schema = @Schema(description = "图片URL", example = "https://example.com/image1.jpg"),
        minItems = 1,
        maxItems = 9,
        arraySchema = @Schema(description = "图片URL列表，至少1张，最多9张", requiredMode = Schema.RequiredMode.REQUIRED)
    )
    @NotEmpty(message = "至少需要上传一张图片")
    @Size(max = 9, message = "最多只能上传9张图片")
    private List<String> images;
    
    /**
     * 经度
     */
    @Schema(description = "经度", example = "121.4737")
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    @Schema(description = "纬度", example = "31.2304")
    private BigDecimal latitude;
    
    /**
     * 地点描述
     */
    @Schema(description = "地点描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "图书馆三楼阅览室")
    @NotBlank(message = "地点描述不能为空")
    private String locationDesc;
    
    /**
     * 丢失/拾获时间
     */
    @Schema(description = "丢失/拾获时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-15T10:30:00")
    @NotNull(message = "丢失/拾获时间不能为空")
    private LocalDateTime eventTime;
}
