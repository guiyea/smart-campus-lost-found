package com.campus.lostandfound.model.enums;

import lombok.Getter;

/**
 * 积分原因枚举
 */
@Getter
public enum PointReason {
    
    /**
     * 每日登录 +2积分
     */
    DAILY_LOGIN("DAILY_LOGIN", "每日登录", 2),
    
    /**
     * 发布招领信息 +10积分
     */
    PUBLISH_FOUND("PUBLISH_FOUND", "发布招领", 10),
    
    /**
     * 帮助找回物品 +50积分
     */
    HELP_FIND("HELP_FIND", "帮助找回", 50);
    
    /**
     * 原因代码
     */
    private final String code;
    
    /**
     * 原因描述
     */
    private final String description;
    
    /**
     * 积分值
     */
    private final int points;
    
    PointReason(String code, String description, int points) {
        this.code = code;
        this.description = description;
        this.points = points;
    }
    
    /**
     * 根据代码获取枚举
     * @param code 原因代码
     * @return 对应的枚举值，未找到返回null
     */
    public static PointReason fromCode(String code) {
        for (PointReason reason : values()) {
            if (reason.getCode().equals(code)) {
                return reason;
            }
        }
        return null;
    }
    
    /**
     * 根据代码获取描述
     * @param code 原因代码
     * @return 对应的描述，未找到返回原代码
     */
    public static String getDescriptionByCode(String code) {
        PointReason reason = fromCode(code);
        return reason != null ? reason.getDescription() : code;
    }
}
