-- =============================================
-- 测试数据初始化脚本
-- 用于开发和测试环境
-- =============================================

USE campuslostandfound;

-- 清空现有数据（避免重复插入错误）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE point_record;
TRUNCATE TABLE message;
TRUNCATE TABLE match_record;
TRUNCATE TABLE item_tag;
TRUNCATE TABLE item_image;
TRUNCATE TABLE item;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 插入测试用户
-- 密码均为: password123 (BCrypt加密后的值)
-- =============================================
INSERT INTO `user` (`student_id`, `name`, `phone`, `password`, `avatar`, `points`, `role`, `status`) VALUES
('2021001', '张三', '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.pjzgxKKmkqEYu', 'https://example.com/avatar1.jpg', 120, 0, 0),
('2021002', '李四', '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.pjzgxKKmkqEYu', 'https://example.com/avatar2.jpg', 80, 0, 0),
('2021003', '王五', '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.pjzgxKKmkqEYu', 'https://example.com/avatar3.jpg', 150, 0, 0),
('ADMIN001', '管理员', '13900139000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.pjzgxKKmkqEYu', 'https://example.com/admin.jpg', 0, 1, 0);

-- =============================================
-- 插入测试失物信息
-- =============================================
INSERT INTO `item` (`user_id`, `title`, `description`, `type`, `category`, `longitude`, `latitude`, `location_desc`, `event_time`, `status`, `view_count`) VALUES
(1, '丢失校园卡', '在图书馆三楼自习时不慎丢失校园卡，卡号以2021开头，卡面有轻微磨损', 0, '证件卡片', 116.397128, 39.909204, '图书馆三楼自习区', '2024-01-15 14:30:00', 0, 25),
(2, '丢失黑色钱包', '黑色皮质钱包，内有身份证、银行卡等重要证件，在食堂附近丢失', 0, '钱包', 116.398128, 39.910204, '第一食堂门口', '2024-01-16 12:00:00', 0, 18),
(3, '丢失蓝牙耳机', 'AirPods Pro，白色充电盒，在操场跑步时丢失', 0, '电子设备', 116.399128, 39.911204, '操场西侧跑道', '2024-01-17 18:00:00', 0, 32);

-- =============================================
-- 插入测试招领信息
-- =============================================
INSERT INTO `item` (`user_id`, `title`, `description`, `type`, `category`, `longitude`, `latitude`, `location_desc`, `event_time`, `status`, `view_count`) VALUES
(2, '拾到校园卡', '在图书馆拾到一张校园卡，卡号2021开头，请失主联系认领', 1, '证件卡片', 116.397228, 39.909304, '图书馆三楼', '2024-01-15 15:00:00', 0, 42),
(3, '拾到钥匙串', '在教学楼A座拾到一串钥匙，有3把钥匙和一个小熊挂件', 1, '钥匙', 116.396128, 39.908204, '教学楼A座一楼大厅', '2024-01-16 09:30:00', 0, 15),
(1, '拾到黑色背包', '在篮球场拾到黑色双肩背包，内有书籍和笔记本', 1, '书籍文具', 116.400128, 39.912204, '篮球场看台', '2024-01-17 16:00:00', 0, 28);

-- =============================================
-- 插入物品图片
-- =============================================
INSERT INTO `item_image` (`item_id`, `url`, `sort`) VALUES
(1, 'https://example.com/items/card1.jpg', 1),
(2, 'https://example.com/items/wallet1.jpg', 1),
(2, 'https://example.com/items/wallet2.jpg', 2),
(3, 'https://example.com/items/airpods1.jpg', 1),
(4, 'https://example.com/items/found_card1.jpg', 1),
(5, 'https://example.com/items/keys1.jpg', 1),
(6, 'https://example.com/items/backpack1.jpg', 1),
(6, 'https://example.com/items/backpack2.jpg', 2);

-- =============================================
-- 插入物品标签（AI识别结果）
-- =============================================
INSERT INTO `item_tag` (`item_id`, `tag`, `confidence`) VALUES
(1, '校园卡', 0.9500),
(1, '蓝色', 0.8800),
(2, '钱包', 0.9200),
(2, '黑色', 0.9000),
(2, '皮质', 0.8500),
(3, '耳机', 0.9300),
(3, 'AirPods', 0.8900),
(3, '白色', 0.9100),
(4, '校园卡', 0.9400),
(4, '蓝色', 0.8700),
(5, '钥匙', 0.9600),
(5, '金属', 0.8800),
(6, '背包', 0.9100),
(6, '黑色', 0.8900);

-- =============================================
-- 插入匹配记录
-- =============================================
INSERT INTO `match_record` (`lost_item_id`, `found_item_id`, `score`, `status`) VALUES
(1, 4, 85.50, 0),  -- 失物1和招领4高度匹配（校园卡）
(2, 6, 45.20, 0);  -- 失物2和招领6部分匹配（钱包vs背包）

-- =============================================
-- 插入消息通知
-- =============================================
INSERT INTO `message` (`user_id`, `title`, `content`, `type`, `related_id`, `is_read`) VALUES
(1, '匹配通知', '您发布的"丢失校园卡"找到了可能的匹配，匹配度85.5%', 1, 1, 0),
(2, '系统通知', '欢迎使用智能校园失物招领平台！', 0, NULL, 1),
(2, '匹配通知', '您发布的"拾到校园卡"找到了可能的失主', 1, 4, 0),
(3, '系统通知', '感谢您发布招领信息，已为您增加10积分', 0, 5, 1);

-- =============================================
-- 插入积分记录
-- =============================================
INSERT INTO `point_record` (`user_id`, `points`, `reason`, `related_id`) VALUES
(1, 2, '每日登录', NULL),
(1, 10, '发布招领信息', 6),
(2, 2, '每日登录', NULL),
(2, 10, '发布招领信息', 4),
(2, 10, '发布招领信息', 5),
(3, 2, '每日登录', NULL),
(3, 10, '发布招领信息', 5);

-- =============================================
-- 测试数据插入完成
-- =============================================
