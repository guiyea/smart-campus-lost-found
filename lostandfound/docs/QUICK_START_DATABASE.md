# 数据库快速启动指南

## 一键初始化

```bash
# 1. 初始化数据库结构
mysql -u root -p < lostandfound/src/main/resources/schema.sql

# 2. 导入测试数据（可选）
mysql -u root -p campuslostandfound < lostandfound/src/main/resources/data-test.sql

# 3. 验证安装
mysql -u root -p < lostandfound/scripts/verify-database.sql
```

## 测试账号

| 学号 | 密码 | 角色 |
|------|------|------|
| 2021001 | password123 | 普通用户 |
| 2021002 | password123 | 普通用户 |
| 2021003 | password123 | 普通用户 |
| ADMIN001 | password123 | 管理员 |

## 数据库连接配置

### 方式一：环境变量

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

### 方式二：application-local.yml

```yaml
spring:
  datasource:
    username: root
    password: your_password
```

## 表结构概览

```
campuslostandfound
├── user (用户表)
├── item (物品信息表)
├── item_image (物品图片表)
├── item_tag (物品标签表)
├── match_record (匹配记录表)
├── message (消息通知表)
└── point_record (积分记录表)
```

## 常用SQL

### 查看所有表
```sql
USE campuslostandfound;
SHOW TABLES;
```

### 查看表结构
```sql
DESC user;
DESC item;
```

### 查看表数据
```sql
SELECT * FROM user LIMIT 10;
SELECT * FROM item WHERE type = 0 LIMIT 10;  -- 失物
SELECT * FROM item WHERE type = 1 LIMIT 10;  -- 招领
```

### 清空测试数据
```sql
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE point_record;
TRUNCATE TABLE message;
TRUNCATE TABLE match_record;
TRUNCATE TABLE item_tag;
TRUNCATE TABLE item_image;
TRUNCATE TABLE item;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;
```

## 故障排查

### 连接失败
```bash
# 检查MySQL服务
# Windows
net start MySQL80

# Linux/Mac
sudo systemctl status mysql
```

### 权限问题
```sql
-- 授予权限
GRANT ALL PRIVILEGES ON campuslostandfound.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

### 字符集问题
```sql
-- 检查字符集
SHOW VARIABLES LIKE 'character%';

-- 应该看到 utf8mb4
```

## 更多信息

- 详细设计：[database-schema.md](./database-schema.md)
- 完整指南：[database-setup-guide.md](./database-setup-guide.md)
- 数据库README：[../src/main/resources/db/README.md](../src/main/resources/db/README.md)
