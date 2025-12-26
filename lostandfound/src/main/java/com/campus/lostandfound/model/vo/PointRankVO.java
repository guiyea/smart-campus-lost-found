package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分排行榜VO
 */
@Schema(description = "积分排行榜信息")
@Data
public class PointRankVO {
    
    /**
     * 排名
     */
    @Schema(description = "排名", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer rank;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;
    
    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;
    
    /**
     * 用户头像URL
     */
    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg", accessMode = Schema.AccessMode.READ_ONLY)
    private String userAvatar;
    
    /**
     * 积分总数
     */
    @Schema(description = "积分总数", example = "500", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer points;
}
