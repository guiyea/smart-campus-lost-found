package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品信息VO
 */
@Schema(description = "物品信息")
@Data
public class ItemVO {
    
    /**
     * 物品ID
     */
    @Schema(description = "物品ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    /**
     * 发布者用户ID
     */
    @Schema(description = "发布者用户ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;
    
    /**
     * 发布者姓名
     */
    @Schema(description = "发布者姓名", example = "张三", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;
    
    /**
     * 发布者头像
     */
    @Schema(description = "发布者头像", example = "https://example.com/avatar.jpg", accessMode = Schema.AccessMode.READ_ONLY)
    private String userAvatar;
    
    /**
     * 标题
     */
    @Schema(description = "标题", example = "丢失一部iPhone 14", accessMode = Schema.AccessMode.READ_ONLY)
    private String title;
    
    /**
     * 描述
     */
    @Schema(description = "描述", example = "在图书馆三楼丢失一部iPhone 14，黑色，有保护壳", accessMode = Schema.AccessMode.READ_ONLY)
    private String description;
    
    /**
     * 类型：0失物 1招领
     */
    @Schema(description = "类型：0-失物, 1-招领", example = "0", allowableValues = {"0", "1"}, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer type;
    
    /**
     * 物品类别
     */
    @Schema(description = "物品类别", example = "电子设备", accessMode = Schema.AccessMode.READ_ONLY)
    private String category;
    
    /**
     * 图片URL列表
     */
    @Schema(description = "图片URL列表", example = "[\"https://example.com/image1.jpg\"]", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> images;
    
    /**
     * 标签列表
     */
    @Schema(description = "标签列表", example = "[\"黑色\", \"iPhone\"]", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> tags;
    
    /**
     * 经度
     */
    @Schema(description = "经度", example = "121.4737", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    @Schema(description = "纬度", example = "31.2304", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal latitude;
    
    /**
     * 地点描述
     */
    @Schema(description = "地点描述", example = "图书馆三楼阅览室", accessMode = Schema.AccessMode.READ_ONLY)
    private String locationDesc;
    
    /**
     * 丢失/拾获时间
     */
    @Schema(description = "丢失/拾获时间", example = "2024-01-15 10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;
    
    /**
     * 状态：0待处理 1已找回 2已关闭
     */
    @Schema(description = "状态：0-待处理, 1-已找回, 2-已关闭", example = "0", allowableValues = {"0", "1", "2"}, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer status;
    
    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数", example = "100", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer viewCount;
    
    /**
     * 删除状态：0未删除 1已删除
     */
    @Schema(description = "删除状态：0-未删除, 1-已删除", example = "0", allowableValues = {"0", "1"}, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer deleted;
    
    /**
     * 距离（计算字段，单位：米）
     */
    @Schema(description = "距离（计算字段，单位：米）", example = "500.5", accessMode = Schema.AccessMode.READ_ONLY)
    private Double distance;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-15 10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-15 10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
