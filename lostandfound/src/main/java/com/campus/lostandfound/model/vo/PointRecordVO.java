package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录VO
 */
@Data
public class PointRecordVO {
    
    /**
     * 积分记录ID
     */
    private Long id;
    
    /**
     * 积分变动(正数为增加,负数为减少)
     */
    private Integer points;
    
    /**
     * 变动原因代码
     */
    private String reason;
    
    /**
     * 变动原因描述
     */
    private String reasonDesc;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    private Long relatedId;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
