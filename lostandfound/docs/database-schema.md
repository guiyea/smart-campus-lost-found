# 数据库设计文档

## 概述

智能校园失物招领平台数据库采用MySQL 8.0，使用utf8mb4字符集，包含7张核心业务表。

## 数据库名称

`campuslostandfound`

## 表结构详细说明

### 1. user (用户表)

存储系统用户的基本信息和状态。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| student_id | VARCHAR(20) | NOT NULL, UNIQUE | 学号/工号 |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| phone | VARCHAR(20) | NOT NULL | 手机号 |
| password | VARCHAR(255) | NOT NULL | 密码(BCrypt加密) |
| avatar | VARCHAR(500) | NULL | 头像URL |
| points | INT | NOT NULL, DEFAULT 0 | 积分 |
| role | TINYINT | NOT NULL, DEFAULT 0 | 角色: 0-普通用户, 1-管理员 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态: 0-正常, 1-封禁 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引:**
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_student_id` (student_id)
- KEY: `idx_phone` (phone)
- KEY: `idx_status` (status)
- KEY: `idx_points` (points DESC) - 用于积分排行榜
- KEY: `idx_created_at` (created_at)

---

### 2. item (物品信息表)

存储失物和招领信息的核心表。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 物品ID |
| user_id | BIGINT | NOT NULL, FK | 发布用户ID |
| title | VARCHAR(100) | NOT NULL | 标题 |
| description | TEXT | NOT NULL | 描述 |
| type | TINYINT | NOT NULL | 类型: 0-失物, 1-招领 |
| category | VARCHAR(50) | NULL | 物品类别 |
| longitude | DECIMAL(10,7) | NULL | 经度 |
| latitude | DECIMAL(10,7) | NULL | 纬度 |
| location_desc | VARCHAR(200) | NOT NULL | 地点描述 |
| event_time | DATETIME | NOT NULL | 丢失/拾获时间 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态: 0-待处理, 1-已找回, 2-已关闭 |
| view_count | INT | NOT NULL, DEFAULT 0 | 浏览次数 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 删除标记: 0-未删除, 1-已删除 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引:**
- PRIMARY KEY: `id`
- KEY: `idx_user_id` (user_id)
- KEY: `idx_type` (type)
- KEY: `idx_category` (category)
- KEY: `idx_status` (status)
- KEY: `idx_deleted` (deleted)
- KEY: `idx_event_time` (event_time)
- KEY: `idx_created_at` (created_at DESC)
- KEY: `idx_location` (longitude, latitude) - 地理位置查询
- KEY: `idx_composite_search` (type, status, deleted, created_at) - 复合查询优化

**外键:**
- `fk_item_user`: user_id REFERENCES user(id) ON DELETE CASCADE

---

### 3. item_image (物品图片表)

存储物品的多张图片信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 图片ID |
| item_id | BIGINT | NOT NULL, FK | 物品ID |
| url | VARCHAR(500) | NOT NULL | 图片URL |
| sort | INT | NOT NULL, DEFAULT 0 | 排序 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引:**
- PRIMARY KEY: `id`
- KEY: `idx_item_id` (item_id)
- KEY: `idx_sort` (item_id, sort) - 按顺序获取图片

**外键:**
- `fk_item_image_item`: item_id REFERENCES item(id) ON DELETE CASCADE

---

### 4. item_tag (物品标签表)

存储AI识别的物品特征标签。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 标签ID |
| item_id | BIGINT | NOT NULL, FK | 物品ID |
| tag | VARCHAR(50) | NOT NULL | 标签名称 |
| confidence | DECIMAL(5,4) | NULL | 置信度(0-1) |

**索引:**
- PRIMARY KEY: `id`
- KEY: `idx_item_id` (item_id)
- KEY: `idx_tag` (tag) - 标签搜索

**外键:**
- `fk_item_tag_item`: item_id REFERENCES item(id) ON DELETE CASCADE

---

### 5. match_record (匹配记录表)

存储失物和招领的匹配关系。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 匹配记录ID |
| lost_item_id | BIGINT | NOT NULL, FK | 失物信息ID |
| found_item_id | BIGINT | NOT NULL, FK | 招领信息ID |
| score | DECIMAL(5,2) | NOT NULL | 匹配分数(0-100) |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态: 0-待确认, 1-已确认, 2-已拒绝 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| confirmed_at | DATETIME | NULL | 确认时间 |

**索引:**
- PRIMARY KEY: `id`
- KEY: `idx_lost_item_id` (lost_item_id)
- KEY: `idx_found_item_id` (found_item_id)
- KEY: `idx_status` (status)
- KEY: `idx_score` (score DESC) - 按匹配度排序
- KEY: `idx_created_at` (created_at)

**外键:**
- `fk_match_lost_item`: lost_item_id REFERENCES item(id) ON DELETE CASCADE
- `fk_match_found_item`: found_item_id REFERENCES item(id) ON DELETE CASCADE

---

### 6. message (消息通知表)

存储站内消息通知。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 消息ID |
| user_id | BIGINT | NOT NULL, FK | 接收用户ID |
| title | VARCHAR(100) | NOT NULL | 消息标题 |
| content | TEXT | NOT NULL | 消息内容 |
| type | TINYINT | NOT NULL | 消息类型: 0-系统通知, 1-匹配通知, 2-留言通知 |
| related_id | BIGINT | NULL | 关联ID(物品ID/匹配ID等) |
| is_read | TINYINT | NOT NULL, DEFAULT 0 | 已读标记: 0-未读, 1-已读 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引:**
- PRIMARY KEY: `id`
- KEY: `idx_user_id` (user_id)
- KEY: `idx_type` (type)
- KEY: `idx_is_read` (is_read)
- KEY: `idx_created_at` (created_at DESC)
- KEY: `idx_user_unread` (user_id, is_read, created_at) - 未读消息查询优化

**外键:**
- `fk_message_user`: user_id REFERENCES user(id) ON DELETE CASCADE

---

### 7. point_record (积分记录表)

记录用户积分变动历史。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 积分记录ID |
| user_id | BIGINT | NOT NULL, FK | 用户ID |
| points | INT | NOT NULL | 积分变动(正数为增加,负数为减少) |
| reason | VARCHAR(100) | NOT NULL | 变动原因 |
| related_id | BIGINT | NULL | 关联ID(物品ID/匹配ID等) |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引:**
- PRIMARY KEY: `id`
- KEY: `idx_user_id` (user_id)
- KEY: `idx_created_at` (created_at DESC)
- KEY: `idx_user_time` (user_id, created_at DESC) - 用户积分明细查询优化

**外键:**
- `fk_point_record_user`: user_id REFERENCES user(id) ON DELETE CASCADE

---

## ER关系图

```
user (1) ----< (N) item
user (1) ----< (N) message
user (1) ----< (N) point_record

item (1) ----< (N) item_image
item (1) ----< (N) item_tag
item (1) ----< (N) match_record (as lost_item)
item (1) ----< (N) match_record (as found_item)
```

## 索引策略

### 1. 主键索引
所有表都使用自增BIGINT作为主键，确保唯一性和高效查询。

### 2. 唯一索引
- user.student_id: 保证学号唯一性，用于登录验证

### 3. 外键索引
所有外键字段都创建了索引，提升关联查询性能。

### 4. 业务索引
- 状态字段索引: 用于筛选不同状态的记录
- 时间字段索引: 支持时间范围查询和排序
- 地理位置索引: 支持LBS范围查询
- 复合索引: 优化常用的多条件查询

## 性能优化建议

1. **分页查询**: 使用LIMIT和OFFSET，避免一次性加载大量数据
2. **软删除**: item表使用deleted字段，避免物理删除导致的外键约束问题
3. **缓存策略**: 热点数据(如积分排行榜)使用Redis缓存
4. **读写分离**: 高并发场景可考虑主从复制
5. **分区表**: 数据量大时可对item、message等表按时间分区

## 数据备份策略

1. **全量备份**: 每天凌晨执行全量备份
2. **增量备份**: 每小时执行binlog增量备份
3. **备份保留**: 全量备份保留30天，增量备份保留7天
4. **异地备份**: 定期将备份文件同步到异地存储

## 安全注意事项

1. **密码加密**: 使用BCrypt算法，密码字段长度255字符
2. **SQL注入防护**: 使用MyBatis-Plus参数化查询
3. **权限控制**: 数据库用户权限最小化原则
4. **敏感数据**: 手机号等敏感信息需要脱敏处理
5. **审计日志**: 记录关键操作的审计日志
