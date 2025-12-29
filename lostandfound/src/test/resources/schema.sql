-- =============================================
-- 智能校园失物招领平台数据库初始化脚本 (H2测试版本)
-- Database: testdb (H2 in-memory)
-- =============================================

-- =============================================
-- 1. 用户表 (user)
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` VARCHAR(20) NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `points` INT NOT NULL DEFAULT 0,
    `role` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`student_id`)
);

-- =============================================
-- 2. 物品信息表 (item)
-- =============================================
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `description` TEXT NOT NULL,
    `type` TINYINT NOT NULL,
    `category` VARCHAR(50) DEFAULT NULL,
    `longitude` DECIMAL(10, 7) DEFAULT NULL,
    `latitude` DECIMAL(10, 7) DEFAULT NULL,
    `location_desc` VARCHAR(200) NOT NULL,
    `event_time` TIMESTAMP NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `view_count` INT NOT NULL DEFAULT 0,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

-- =============================================
-- 3. 物品图片表 (item_image)
-- =============================================
CREATE TABLE IF NOT EXISTS `item_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `item_id` BIGINT NOT NULL,
    `url` VARCHAR(500) NOT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
);

-- =============================================
-- 4. 物品标签表 (item_tag)
-- =============================================
CREATE TABLE IF NOT EXISTS `item_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `item_id` BIGINT NOT NULL,
    `tag` VARCHAR(50) NOT NULL,
    `confidence` DECIMAL(5, 4) DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
);

-- =============================================
-- 5. 匹配记录表 (match_record)
-- =============================================
CREATE TABLE IF NOT EXISTS `match_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `lost_item_id` BIGINT NOT NULL,
    `found_item_id` BIGINT NOT NULL,
    `score` DECIMAL(5, 2) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `confirmed_at` TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`lost_item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`found_item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
);

-- =============================================
-- 6. 消息通知表 (message)
-- =============================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` TEXT NOT NULL,
    `type` TINYINT NOT NULL,
    `related_id` BIGINT DEFAULT NULL,
    `is_read` TINYINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

-- =============================================
-- 7. 积分记录表 (point_record)
-- =============================================
CREATE TABLE IF NOT EXISTS `point_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `points` INT NOT NULL,
    `reason` VARCHAR(100) NOT NULL,
    `related_id` BIGINT DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

-- =============================================
-- 8. 匹配反馈表 (match_feedback)
-- =============================================
CREATE TABLE IF NOT EXISTS `match_feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `item_id` BIGINT NOT NULL,
    `matched_item_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `is_accurate` TINYINT NOT NULL,
    `comment` TEXT DEFAULT NULL,
    `match_score` DECIMAL(5, 2) DEFAULT NULL,
    `item_category` VARCHAR(50) DEFAULT NULL,
    `matched_item_category` VARCHAR(50) DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`matched_item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

-- =============================================
-- 插入测试管理员用户
-- =============================================
INSERT INTO `user` (`student_id`, `name`, `phone`, `password`, `points`, `role`, `status`)
VALUES ('admin001', '系统管理员', '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 0, 1, 0);
