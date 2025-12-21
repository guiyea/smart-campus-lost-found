# 数据库初始化说明

## 数据库结构

智能校园失物招领平台使用MySQL 8.0作为主数据库，包含以下7张核心表：

### 1. user (用户表)
- 存储用户基本信息、积分、角色和状态
- 学号(student_id)作为唯一标识用于登录
- 支持BCrypt密码加密存储

### 2. item (物品信息表)
- 存储失物和招领信息
- 包含地理位置信息(经纬度)
- 支持软删除(deleted字段)

### 3. item_image (物品图片表)
- 存储物品的多张图片URL
- 支持图片排序

### 4. item_tag (物品标签表)
- 存储AI识别的物品特征标签
- 包含置信度信息

### 5. match_record (匹配记录表)
- 存储失物和招领的匹配关系
- 记录匹配分数和确认状态

### 6. message (消息通知表)
- 存储站内消息通知
- 支持系统通知、匹配通知、留言通知

### 7. point_record (积分记录表)
- 记录用户积分变动历史
- 关联具体的操作原因

## 初始化步骤

### 方式一：使用MySQL命令行

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 执行初始化脚本
source /path/to/lostandfound/src/main/resources/schema.sql

# 3. 验证表创建
USE campuslostandfound;
SHOW TABLES;
```

### 方式二：使用MySQL Workbench

1. 打开MySQL Workbench
2. 连接到MySQL服务器
3. 打开 `schema.sql` 文件
4. 点击执行按钮运行脚本

### 方式三：使用命令行直接执行

```bash
mysql -u root -p < lostandfound/src/main/resources/schema.sql
```

## 环境变量配置

在运行应用前，需要配置以下数据库相关环境变量：

```bash
# 数据库配置
export DB_USERNAME=root
export DB_PASSWORD=your_password

# 或者在 application-local.yml 中配置
```

## 验证安装

执行以下SQL验证表结构：

```sql
USE campuslostandfound;

-- 查看所有表
SHOW TABLES;

-- 查看user表结构
DESC user;

-- 查看item表索引
SHOW INDEX FROM item;

-- 验证外键约束
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = 'campuslostandfound'
    AND REFERENCED_TABLE_NAME IS NOT NULL;
```

## 注意事项

1. **字符集**: 所有表使用 `utf8mb4` 字符集，支持emoji和特殊字符
2. **时区**: 数据库连接URL中指定了 `serverTimezone=Asia/Shanghai`
3. **外键约束**: 所有外键设置为 `ON DELETE CASCADE`，删除主记录时自动删除关联记录
4. **软删除**: item表使用 `deleted` 字段实现软删除，MyBatis-Plus会自动处理
5. **索引优化**: 为常用查询字段和外键创建了索引，提升查询性能

## 测试数据

如需插入测试数据，可以创建 `data.sql` 文件并执行。示例：

```sql
-- 插入测试用户
INSERT INTO user (student_id, name, phone, password, points, role, status) 
VALUES 
('2021001', '张三', '13800138000', '$2a$10$...', 100, 0, 0),
('2021002', '李四', '13800138001', '$2a$10$...', 50, 0, 0);

-- 插入测试物品
INSERT INTO item (user_id, title, description, type, category, location_desc, event_time, status) 
VALUES 
(1, '丢失校园卡', '在图书馆丢失校园卡', 0, '证件卡片', '图书馆三楼', NOW(), 0);
```

## 数据库迁移

如需修改表结构，建议：
1. 创建新的迁移脚本 `migration_v2.sql`
2. 使用 `ALTER TABLE` 语句而非 `DROP TABLE`
3. 保留历史数据，避免数据丢失
