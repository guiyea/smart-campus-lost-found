package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品信息VO
 */
@Data
public class ItemVO {
    
    /**
     * 物品ID
     */
    private Long id;
    
    /**
     * 发布者用户ID
     */
    private Long userId;
    
    /**
     * 发布者姓名
     */
    private String userName;
    
    /**
     * 发布者头像
     */
    private String userAvatar;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 类型：0失物 1招领
     */
    private Integer type;
    
    /**
     * 物品类别
     */
    private String category;
    
    /**
     * 图片URL列表
     */
    private List<String> images;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
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
    private String locationDesc;
    
    /**
     * 丢失/拾获时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;
    
    /**
     * 状态：0待处理 1已找回 2已关闭
     */
    private Integer status;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 距离（计算字段，单位：米）
     */
    private Double distance;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
