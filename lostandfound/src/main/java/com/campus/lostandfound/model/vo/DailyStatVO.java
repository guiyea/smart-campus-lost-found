package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每日统计数据VO
 */
@Schema(description = "每日统计数据")
@Data
public class DailyStatVO {
    
    /**
     * 日期
     */
    @Schema(description = "日期", example = "2024-01-15", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate date;
    
    /**
     * 新增用户数
     */
    @Schema(description = "新增用户数", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer newUsers;
    
    /**
     * 新增信息数
     */
    @Schema(description = "新增信息数", example = "50", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer newItems;
    
    /**
     * 匹配成功数
     */
    @Schema(description = "匹配成功数", example = "15", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer matchedCount;
}
