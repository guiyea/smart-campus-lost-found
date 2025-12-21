-- =============================================
-- 智能校园失物招领平台数据库初始化脚本
-- Database: campuslostandfound
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS campuslostandfound
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campuslostandfound;

-- =============================================
-- 1. 用户表 (user)
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `student_id` VARCHAR(20) NOT NULL COMMENT '学号/工号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-正常, 1-封禁',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_points` (`points` DESC),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 物品信息表 (item)
-- =============================================
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` TEXT NOT NULL COMMENT '描述',
    `type` TINYINT NOT NULL COMMENT '类型: 0-失物, 1-招领',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '物品类别',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    `location_desc` VARCHAR(200) NOT NULL COMMENT '地点描述',
    `event_time` DATETIME NOT NULL COMMENT '丢失/拾获时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-已找回, 2-已关闭',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_event_time` (`event_time`),
    KEY `idx_created_at` (`created_at` DESC),
    KEY `idx_location` (`longitude`, `latitude`),
    KEY `idx_composite_search` (`type`, `status`, `deleted`, `created_at`),
    CONSTRAINT `fk_item_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品信息表';

-- =============================================
-- 3. 物品图片表 (item_image)
-- =============================================
CREATE TABLE IF NOT EXISTS `item_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_sort` (`item_id`, `sort`),
    CONSTRAINT `fk_item_image_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品图片表';

-- =============================================
-- 4. 物品标签表 (item_tag)
-- =============================================
CREATE TABLE IF NOT EXISTS `item_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `tag` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `confidence` DECIMAL(5, 4) DEFAULT NULL COMMENT '置信度(0-1)',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_tag` (`tag`),
    CONSTRAINT `fk_item_tag_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品标签表';

-- =============================================
-- 5. 匹配记录表 (match_record)
-- =============================================
CREATE TABLE IF NOT EXISTS `match_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '匹配记录ID',
    `lost_item_id` BIGINT NOT NULL COMMENT '失物信息ID',
    `found_item_id` BIGINT NOT NULL COMMENT '招领信息ID',
    `score` DECIMAL(5, 2) NOT NULL COMMENT '匹配分数(0-100)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待确认, 1-已确认, 2-已拒绝',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `confirmed_at` DATETIME DEFAULT NULL COMMENT '确认时间',
    PRIMARY KEY (`id`),
    KEY `idx_lost_item_id` (`lost_item_id`),
    KEY `idx_found_item_id` (`found_item_id`),
    KEY `idx_status` (`status`),
    KEY `idx_score` (`score` DESC),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_match_lost_item` FOREIGN KEY (`lost_item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_match_found_item` FOREIGN KEY (`found_item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='匹配记录表';

-- =============================================
-- 6. 消息通知表 (message)
-- =============================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT NOT NULL COMMENT '消息类型: 0-系统通知, 1-匹配通知, 2-留言通知',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID(物品ID/匹配ID等)',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '已读标记: 0-未读, 1-已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_at` (`created_at` DESC),
    KEY `idx_user_unread` (`user_id`, `is_read`, `created_at`),
    CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- =============================================
-- 7. 积分记录表 (point_record)
-- =============================================
CREATE TABLE IF NOT EXISTS `point_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '积分记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `points` INT NOT NULL COMMENT '积分变动(正数为增加,负数为减少)',
    `reason` VARCHAR(100) NOT NULL COMMENT '变动原因',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID(物品ID/匹配ID等)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at` DESC),
    KEY `idx_user_time` (`user_id`, `created_at` DESC),
    CONSTRAINT `fk_point_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- =============================================
-- 索引说明
-- =============================================
-- user表:
--   - uk_student_id: 学号唯一索引,用于登录和注册验证
--   - idx_phone: 手机号索引,用于查询和验证
--   - idx_status: 状态索引,用于筛选正常/封禁用户
--   - idx_points: 积分降序索引,用于积分排行榜
--   - idx_created_at: 创建时间索引,用于统计新增用户
--
-- item表:
--   - idx_user_id: 用户ID索引,用于查询用户发布的物品
--   - idx_type: 类型索引,用于区分失物/招领
--   - idx_category: 类别索引,用于分类筛选
--   - idx_status: 状态索引,用于筛选待处理/已找回
--   - idx_deleted: 删除标记索引,用于过滤已删除记录
--   - idx_event_time: 事件时间索引,用于时间范围查询
--   - idx_created_at: 创建时间降序索引,用于最新发布排序
--   - idx_location: 经纬度复合索引,用于地理位置查询
--   - idx_composite_search: 复合索引,优化常用搜索条件组合
--
-- item_image表:
--   - idx_item_id: 物品ID索引,用于查询物品的所有图片
--   - idx_sort: 物品ID+排序复合索引,用于按顺序获取图片
--
-- item_tag表:
--   - idx_item_id: 物品ID索引,用于查询物品的所有标签
--   - idx_tag: 标签索引,用于标签搜索
--
-- match_record表:
--   - idx_lost_item_id: 失物ID索引,用于查询失物的匹配记录
--   - idx_found_item_id: 招领ID索引,用于查询招领的匹配记录
--   - idx_status: 状态索引,用于筛选确认状态
--   - idx_score: 分数降序索引,用于按匹配度排序
--   - idx_created_at: 创建时间索引,用于时间排序
--
-- message表:
--   - idx_user_id: 用户ID索引,用于查询用户的所有消息
--   - idx_type: 类型索引,用于按类型筛选消息
--   - idx_is_read: 已读标记索引,用于筛选未读消息
--   - idx_created_at: 创建时间降序索引,用于时间排序
--   - idx_user_unread: 用户+已读+时间复合索引,优化未读消息查询
--
-- point_record表:
--   - idx_user_id: 用户ID索引,用于查询用户的积分记录
--   - idx_created_at: 创建时间降序索引,用于时间排序
--   - idx_user_time: 用户+时间复合索引,优化用户积分明细查询

-- =============================================
-- 初始化完成
-- =============================================
