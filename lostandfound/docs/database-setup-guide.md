# 数据库设置指南

## 概述

本指南详细说明如何为智能校园失物招领平台设置MySQL数据库。

## 前置要求

- MySQL 8.0 或更高版本
- 数据库管理工具（MySQL Workbench、命令行或其他）
- 足够的数据库权限（CREATE DATABASE, CREATE TABLE等）

## 快速开始

### 1. 创建数据库并初始化表结构

```bash
# 方式一：使用MySQL命令行
mysql -u root -p < lostandfound/src/main/resources/schema.sql

# 方式二：分步执行
mysql -u root -p
mysql> source /path/to/lostandfound/src/main/resources/schema.sql
```

### 2. 验证数据库设置

```bash
mysql -u root -p < lostandfound/scripts/verify-database.sql
```

### 3. （可选）导入测试数据

```bash
mysql -u root -p campuslostandfound < lostandfound/src/main/resources/data-test.sql
```

## 详细步骤

### 步骤1：准备MySQL环境

确保MySQL服务正在运行：

```bash
# Windows
net start MySQL80

# Linux/Mac
sudo systemctl start mysql
# 或
sudo service mysql start
```

### 步骤2：创建数据库用户（可选）

为应用创建专用数据库用户：

```sql
-- 创建用户
CREATE USER 'campus_app'@'localhost' IDENTIFIED BY 'your_secure_password';

-- 授予权限
GRANT ALL PRIVILEGES ON campuslostandfound.* TO 'campus_app'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 步骤3：执行初始化脚本

`schema.sql` 脚本会自动：
1. 创建数据库 `campuslostandfound`（如果不存在）
2. 设置字符集为 `utf8mb4`
3. 创建7张核心业务表
4. 创建所有必要的索引
5. 设置外键约束

### 步骤4：验证表结构

执行以下SQL验证：

```sql
USE campuslostandfound;

-- 检查表数量（应该是7张）
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'campuslostandfound';

-- 查看所有表
SHOW TABLES;

-- 检查user表结构
DESC user;

-- 检查item表索引
SHOW INDEX FROM item;
```

预期结果：
- 7张表：user, item, item_image, item_tag, match_record, message, point_record
- 每张表都有主键和相应的索引
- 外键约束正确设置

## 表结构说明

### 核心表

| 表名 | 说明 | 记录数预估 |
|------|------|-----------|
| user | 用户信息 | 10,000+ |
| item | 物品信息 | 50,000+ |
| item_image | 物品图片 | 200,000+ |
| item_tag | 物品标签 | 150,000+ |
| match_record | 匹配记录 | 100,000+ |
| message | 消息通知 | 500,000+ |
| point_record | 积分记录 | 200,000+ |

### 关键字段说明

#### user表
- `student_id`: 学号/工号，唯一标识，用于登录
- `password`: BCrypt加密存储，长度255
- `points`: 用户积分，用于排行榜
- `role`: 0-普通用户, 1-管理员
- `status`: 0-正常, 1-封禁

#### item表
- `type`: 0-失物, 1-招领
- `status`: 0-待处理, 1-已找回, 2-已关闭
- `deleted`: 软删除标记，0-未删除, 1-已删除
- `longitude/latitude`: 地理位置坐标，支持LBS查询

## 配置应用连接

### application.yml配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslostandfound?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 环境变量配置

```bash
# Linux/Mac
export DB_USERNAME=campus_app
export DB_PASSWORD=your_secure_password

# Windows
set DB_USERNAME=campus_app
set DB_PASSWORD=your_secure_password
```

### application-local.yml配置

创建 `src/main/resources/application-local.yml`（不提交到版本控制）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslostandfound?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_local_password
```

## 测试数据

### 导入测试数据

```bash
mysql -u root -p campuslostandfound < lostandfound/src/main/resources/data-test.sql
```

测试数据包括：
- 4个测试用户（包括1个管理员）
- 6条物品信息（3条失物，3条招领）
- 相关的图片、标签、匹配记录
- 示例消息和积分记录

### 测试用户凭据

所有测试用户的密码都是：`password123`

| 学号 | 姓名 | 角色 | 积分 |
|------|------|------|------|
| 2021001 | 张三 | 普通用户 | 120 |
| 2021002 | 李四 | 普通用户 | 80 |
| 2021003 | 王五 | 普通用户 | 150 |
| ADMIN001 | 管理员 | 管理员 | 0 |

## 性能优化

### 索引优化

已创建的关键索引：
- 用户表：学号唯一索引、积分降序索引
- 物品表：复合查询索引、地理位置索引
- 消息表：用户未读消息复合索引
- 所有外键字段都有索引

### 查询优化建议

1. **使用索引字段查询**
   ```sql
   -- 好：使用索引
   SELECT * FROM item WHERE type = 0 AND status = 0 AND deleted = 0;
   
   -- 避免：全表扫描
   SELECT * FROM item WHERE YEAR(created_at) = 2024;
   ```

2. **分页查询**
   ```sql
   -- 使用LIMIT分页
   SELECT * FROM item ORDER BY created_at DESC LIMIT 20 OFFSET 0;
   ```

3. **避免SELECT ***
   ```sql
   -- 只查询需要的字段
   SELECT id, title, type, status FROM item WHERE user_id = 1;
   ```

### 连接池配置

在 `application.yml` 中已配置HikariCP：
- 最小空闲连接：5
- 最大连接数：20
- 连接超时：30秒
- 最大存活时间：30分钟

## 备份与恢复

### 备份数据库

```bash
# 完整备份
mysqldump -u root -p campuslostandfound > backup_$(date +%Y%m%d).sql

# 仅备份结构
mysqldump -u root -p --no-data campuslostandfound > schema_backup.sql

# 仅备份数据
mysqldump -u root -p --no-create-info campuslostandfound > data_backup.sql
```

### 恢复数据库

```bash
# 恢复完整备份
mysql -u root -p campuslostandfound < backup_20240115.sql

# 先删除再重建
mysql -u root -p -e "DROP DATABASE IF EXISTS campuslostandfound;"
mysql -u root -p < backup_20240115.sql
```

## 故障排查

### 常见问题

#### 1. 字符集问题

**症状**：中文显示乱码

**解决**：
```sql
-- 检查字符集
SHOW VARIABLES LIKE 'character%';

-- 修改表字符集
ALTER TABLE user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 连接超时

**症状**：应用无法连接数据库

**解决**：
- 检查MySQL服务是否运行
- 检查防火墙设置
- 验证用户名密码
- 检查 `max_connections` 配置

```sql
SHOW VARIABLES LIKE 'max_connections';
SET GLOBAL max_connections = 200;
```

#### 3. 外键约束错误

**症状**：无法删除或更新记录

**解决**：
```sql
-- 临时禁用外键检查（谨慎使用）
SET FOREIGN_KEY_CHECKS = 0;
-- 执行操作
-- ...
SET FOREIGN_KEY_CHECKS = 1;
```

#### 4. 索引未生效

**症状**：查询很慢

**解决**：
```sql
-- 分析查询
EXPLAIN SELECT * FROM item WHERE type = 0;

-- 重建索引
ALTER TABLE item DROP INDEX idx_type;
ALTER TABLE item ADD INDEX idx_type (type);

-- 优化表
OPTIMIZE TABLE item;
```

## 监控与维护

### 定期维护任务

1. **每日**：检查慢查询日志
2. **每周**：优化表、更新统计信息
3. **每月**：检查索引使用情况、清理过期数据

### 监控SQL

```sql
-- 查看表大小
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'campuslostandfound'
ORDER BY size_mb DESC;

-- 查看索引使用情况
SELECT * FROM sys.schema_unused_indexes
WHERE object_schema = 'campuslostandfound';

-- 查看慢查询
SELECT * FROM mysql.slow_log
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY);
```

## 安全建议

1. **密码策略**
   - 使用强密码
   - 定期更换密码
   - 不在代码中硬编码密码

2. **权限控制**
   - 应用使用专用数据库用户
   - 最小权限原则
   - 禁止root用户远程访问

3. **网络安全**
   - 限制数据库访问IP
   - 使用SSL连接（生产环境）
   - 配置防火墙规则

4. **数据安全**
   - 定期备份
   - 敏感数据加密
   - 审计日志记录

## 参考资料

- [MySQL 8.0 官方文档](https://dev.mysql.com/doc/refman/8.0/en/)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [数据库设计文档](./database-schema.md)
