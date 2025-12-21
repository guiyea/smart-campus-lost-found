# Windows 环境快速设置指南

## 前置要求

- ✅ MySQL 8.0 已安装
- ✅ Java 21 已安装
- ✅ Maven 已安装（或使用项目自带的 mvnw）

## 第一步：启动 MySQL 服务

### 方式一：使用命令行

```cmd
net start MySQL80
```

### 方式二：使用服务管理器

1. 按 `Win + R`，输入 `services.msc`
2. 找到 MySQL80 服务
3. 右键点击 -> 启动

## 第二步：测试数据库连接

在项目根目录下运行：

```cmd
cd lostandfound\scripts
test-connection.bat
```

如果连接成功，会显示 MySQL 版本和当前时间。

## 第三步：初始化数据库

### 自动初始化（推荐）

```cmd
cd lostandfound\scripts
init-database.bat
```

脚本会自动：
1. 创建数据库 `campuslostandfound`
2. 创建所有表结构
3. 询问是否导入测试数据
4. 验证安装

### 手动初始化

```cmd
# 进入MySQL命令行
mysql -uroot -p40619128

# 执行初始化脚本
source C:/path/to/lostandfound/src/main/resources/schema.sql

# 导入测试数据（可选）
use campuslostandfound;
source C:/path/to/lostandfound/src/main/resources/data-test.sql
```

## 第四步：配置应用

配置文件 `application-local.yml` 已经配置好：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslostandfound
    username: root
    password: 40619128
```

## 第五步：启动应用

### 使用 Maven Wrapper（推荐）

```cmd
cd lostandfound
mvnw.cmd spring-boot:run
```

### 使用 Maven

```cmd
cd lostandfound
mvn spring-boot:run
```

### 使用 IDE

1. 在 IDEA 或 Eclipse 中打开项目
2. 找到 `LostandfoundApplication.java`
3. 右键 -> Run

## 验证安装

### 1. 检查应用是否启动

访问：http://localhost:8080

### 2. 检查数据库

```cmd
mysql -uroot -p40619128 -e "USE campuslostandfound; SHOW TABLES;"
```

应该看到 7 张表：
- user
- item
- item_image
- item_tag
- match_record
- message
- point_record

### 3. 测试账号

如果导入了测试数据，可以使用以下账号登录：

| 学号 | 密码 | 角色 |
|------|------|------|
| 2021001 | password123 | 普通用户 |
| 2021002 | password123 | 普通用户 |
| 2021003 | password123 | 普通用户 |
| ADMIN001 | password123 | 管理员 |

## 常见问题

### 问题 1：MySQL 服务启动失败

**错误信息**：`服务名无效` 或 `找不到服务`

**解决方案**：
1. 检查 MySQL 服务名称：
   ```cmd
   sc query | findstr MySQL
   ```
2. 使用正确的服务名启动：
   ```cmd
   net start [服务名]
   ```

### 问题 2：连接被拒绝

**错误信息**：`Access denied for user 'root'@'localhost'`

**解决方案**：
1. 确认密码是否正确
2. 重置 root 密码：
   ```cmd
   mysqladmin -u root -p password 新密码
   ```

### 问题 3：端口被占用

**错误信息**：`Port 8080 was already in use`

**解决方案**：
1. 查找占用端口的进程：
   ```cmd
   netstat -ano | findstr :8080
   ```
2. 结束进程：
   ```cmd
   taskkill /PID [进程ID] /F
   ```
3. 或修改应用端口（在 application.yml 中）

### 问题 4：找不到 MySQL 命令

**错误信息**：`'mysql' 不是内部或外部命令`

**解决方案**：
1. 找到 MySQL 安装目录（通常在 `C:\Program Files\MySQL\MySQL Server 8.0\bin`）
2. 添加到系统 PATH：
   - 右键 "此电脑" -> 属性
   - 高级系统设置 -> 环境变量
   - 在系统变量中找到 Path，点击编辑
   - 添加 MySQL 的 bin 目录路径
   - 重启命令行窗口

### 问题 5：字符集问题

**症状**：中文显示乱码

**解决方案**：
1. 检查 MySQL 字符集：
   ```sql
   SHOW VARIABLES LIKE 'character%';
   ```
2. 确保都是 utf8mb4
3. 如果不是，修改 MySQL 配置文件 `my.ini`：
   ```ini
   [mysqld]
   character-set-server=utf8mb4
   collation-server=utf8mb4_unicode_ci
   
   [client]
   default-character-set=utf8mb4
   ```

## 开发工具推荐

### 数据库管理工具
- **MySQL Workbench**（官方工具）
- **Navicat**（商业软件）
- **DBeaver**（免费开源）
- **HeidiSQL**（免费）

### Java IDE
- **IntelliJ IDEA**（推荐）
- **Eclipse**
- **VS Code** + Java 扩展

### API 测试工具
- **Postman**
- **Apifox**
- **Insomnia**

## 下一步

1. ✅ 数据库已初始化
2. ✅ 应用已启动
3. 📝 开始实现业务功能（参考 tasks.md）
4. 🧪 编写测试用例
5. 📚 查看 API 文档：http://localhost:8080/swagger-ui.html

## 获取帮助

- 查看项目文档：`lostandfound/docs/`
- 查看需求文档：`.kiro/specs/smart-campus-lost-found/requirements.md`
- 查看设计文档：`.kiro/specs/smart-campus-lost-found/design.md`
- 查看任务列表：`.kiro/specs/smart-campus-lost-found/tasks.md`

## 清理和重置

### 重置数据库

```cmd
mysql -uroot -p40619128 -e "DROP DATABASE IF EXISTS campuslostandfound;"
cd lostandfound\scripts
init-database.bat
```

### 清理 Maven 缓存

```cmd
cd lostandfound
mvnw.cmd clean
```

### 完全重新开始

```cmd
# 1. 删除数据库
mysql -uroot -p40619128 -e "DROP DATABASE IF EXISTS campuslostandfound;"

# 2. 清理编译文件
cd lostandfound
mvnw.cmd clean

# 3. 重新初始化
cd scripts
init-database.bat

# 4. 重新编译运行
cd ..
mvnw.cmd spring-boot:run
```
