package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录VO
 */
@Schema(description = "积分记录信息")
@Data
public class PointRecordVO {
    
    /**
     * 积分记录ID
     */
    @Schema(description = "积分记录ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    /**
     * 积分变动(正数为增加,负数为减少)
     */
    @Schema(description = "积分变动(正数为增加,负数为减少)", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer points;
    
    /**
     * 变动原因代码
     */
    @Schema(description = "变动原因代码", example = "PUBLISH_ITEM", accessMode = Schema.AccessMode.READ_ONLY)
    private String reason;
    
    /**
     * 变动原因描述
     */
    @Schema(description = "变动原因描述", example = "发布物品", accessMode = Schema.AccessMode.READ_ONLY)
    private String reasonDesc;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    @Schema(description = "关联ID(物品ID/匹配ID等)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long relatedId;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-15 10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
