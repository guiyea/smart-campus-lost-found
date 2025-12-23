package com.campus.lostandfound.model.dto;

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
@Data
public class ItemDTO {
    
    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 50, message = "标题长度必须在2-50个字符之间")
    private String title;
    
    /**
     * 描述
     */
    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 1000, message = "描述长度必须在10-1000个字符之间")
    private String description;
    
    /**
     * 类型：0失物 1招领
     */
    @NotNull(message = "类型不能为空")
    private Integer type;
    
    /**
     * 物品类别（可由AI识别自动填充）
     */
    private String category;
    
    /**
     * 图片URL列表
     */
    @NotEmpty(message = "至少需要上传一张图片")
    @Size(max = 9, message = "最多只能上传9张图片")
    private List<String> images;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 地点描述
     */
    @NotBlank(message = "地点描述不能为空")
    private String locationDesc;
    
    /**
     * 丢失/拾获时间
     */
    @NotNull(message = "丢失/拾获时间不能为空")
    private LocalDateTime eventTime;
}
