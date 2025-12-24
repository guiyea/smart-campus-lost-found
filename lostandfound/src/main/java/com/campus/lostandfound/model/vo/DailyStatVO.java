package com.campus.lostandfound.model.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 每日统计数据VO
 */
@Data
public class DailyStatVO {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 新增用户数
     */
    private Integer newUsers;
    
    /**
     * 新增信息数
     */
    private Integer newItems;
    
    /**
     * 匹配成功数
     */
    private Integer matchedCount;
}
