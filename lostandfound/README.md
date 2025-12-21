# 智能校园失物招领平台 - 后端服务

> 💡 **Windows 用户快速开始**：查看 [Windows 环境设置指南](docs/WINDOWS_SETUP.md)

## 项目状态

✅ **已完成**
- 项目初始化与依赖配置
- 数据库表结构设计与创建
- MyBatis-Plus基础代码生成（Entity + Mapper）
- 分页插件配置
- 自动填充时间字段配置

🚧 **进行中**
- 统一响应与异常处理
- 用户认证模块（JWT + Spring Security）

📋 **待开发**
- 文件上传模块
- 物品信息模块
- AI图像识别模块
- LBS地理服务模块
- 智能匹配模块
- 消息通知模块
- 积分模块
- 管理后台模块

## 项目结构

```
lostandfound/
├── .mvn/                            # Maven Wrapper配置
│   └── wrapper/
│       └── maven-wrapper.properties
├── docs/                            # 项目文档
│   ├── database-schema.md          # 数据库设计文档
│   ├── database-setup-guide.md     # 数据库设置指南
│   ├── QUICK_START_DATABASE.md     # 数据库快速开始
│   └── WINDOWS_SETUP.md            # Windows环境设置
├── scripts/                         # 脚本文件
│   ├── init-database.bat           # 数据库初始化脚本（Windows）
│   ├── test-connection.bat         # 数据库连接测试（Windows）
│   └── verify-database.sql         # 数据库验证SQL
├── src/
│   ├── main/
│   │   ├── java/com/campus/lostandfound/
│   │   │   ├── config/              # ✅ 配置类
│   │   │   │   └── MyBatisPlusConfig.java  # MyBatis-Plus配置
│   │   │   ├── controller/          # 控制器层（REST API接口）
│   │   │   ├── service/             # 服务层接口
│   │   │   │   └── impl/            # 服务层实现
│   │   │   ├── repository/          # ✅ 数据访问层（MyBatis Mapper）
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── ItemMapper.java
│   │   │   │   ├── ItemImageMapper.java
│   │   │   │   ├── ItemTagMapper.java
│   │   │   │   ├── MatchRecordMapper.java
│   │   │   │   ├── MessageMapper.java
│   │   │   │   └── PointRecordMapper.java
│   │   │   ├── model/               # 数据模型
│   │   │   │   ├── entity/          # ✅ 实体类（数据库表映射）
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Item.java
│   │   │   │   │   ├── ItemImage.java
│   │   │   │   │   ├── ItemTag.java
│   │   │   │   │   ├── MatchRecord.java
│   │   │   │   │   ├── Message.java
│   │   │   │   │   └── PointRecord.java
│   │   │   │   ├── dto/             # 数据传输对象（请求参数）
│   │   │   │   └── vo/              # 视图对象（响应数据）
│   │   │   ├── common/              # 公共组件（统一响应、常量等）
│   │   │   ├── exception/           # 异常处理
│   │   │   ├── util/                # 工具类（JWT, Redis等）
│   │   │   └── websocket/           # WebSocket处理器
│   │   └── resources/
│   │       ├── db/                  # 数据库相关文档
│   │       ├── mapper/              # MyBatis XML映射文件
│   │       ├── static/              # 静态资源
│   │       ├── templates/           # 模板文件
│   │       ├── application.yml      # ✅ 主配置文件
│   │       ├── application.properties # 配置文件
│   │       ├── application-local.yml # ✅ 本地开发配置（不提交到VCS）
│   │       ├── schema.sql           # ✅ 数据库建表脚本
│   │       └── data-test.sql        # ✅ 测试数据脚本
│   └── test/                        # 测试代码
│       └── java/com/campus/lostandfound/
│           └── LostandfoundApplicationTests.java
├── .gitignore                       # Git忽略配置
├── mvnw                             # Maven Wrapper脚本（Linux/Mac）
├── mvnw.cmd                         # Maven Wrapper脚本（Windows）
├── pom.xml                          # ✅ Maven项目配置
├── README.md                        # 项目说明文档
└── 快速开始.md                      # 快速开始指南（中文）
```

**图例说明**
- ✅ 已完成/已配置的文件
- 📁 目录结构已创建，等待实现

## 技术栈

- **框架**: Spring Boot 4.0.1
- **Java版本**: Java 21
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **ORM**: MyBatis-Plus 3.5.5
- **安全**: Spring Security + JWT
- **文件存储**: 阿里云OSS
- **AI服务**: 百度AI图像识别
- **地图服务**: 高德地图API
- **测试框架**: JUnit 5 + jqwik (属性测试)

## 依赖说明

### 核心依赖
- `spring-boot-starter-web`: Web应用开发
- `spring-boot-starter-security`: 安全认证
- `spring-boot-starter-validation`: 参数验证
- `spring-boot-starter-data-redis`: Redis集成
- `mybatis-plus-boot-starter`: MyBatis-Plus ORM
- `mysql-connector-j`: MySQL驱动
- `lombok`: 简化Java代码

### JWT认证
- `jjwt-api`: JWT API
- `jjwt-impl`: JWT实现
- `jjwt-jackson`: JWT JSON处理

### 测试依赖
- `spring-boot-starter-test`: Spring Boot测试
- `spring-boot-starter-security-test`: Security测试
- `jqwik`: 属性测试框架

## 数据模型

### 实体类（Entity）

已生成7个实体类，映射数据库表结构：

| 实体类 | 数据库表 | 说明 |
|--------|---------|------|
| `User` | user | 用户信息 |
| `Item` | item | 物品信息（失物/招领）|
| `ItemImage` | item_image | 物品图片 |
| `ItemTag` | item_tag | 物品标签（AI识别） |
| `MatchRecord` | match_record | 匹配记录 |
| `Message` | message | 消息通知 |
| `PointRecord` | point_record | 积分记录 |

### Mapper接口

所有Mapper接口继承 `BaseMapper<T>`，提供基础CRUD操作：
- `UserMapper`
- `ItemMapper`
- `ItemImageMapper`
- `ItemTagMapper`
- `MatchRecordMapper`
- `MessageMapper`
- `PointRecordMapper`

### MyBatis-Plus配置

**分页插件**
- 数据库类型：MySQL
- 最大单页限制：500条
- 溢出处理：关闭

**自动填充**
- `createdAt`: 插入时自动填充当前时间
- `updatedAt`: 插入和更新时自动填充当前时间

**软删除**
- `Item.deleted`: 使用 `@TableLogic` 注解实现软删除
- 查询时自动过滤已删除记录
- 删除操作自动转换为更新操作

## 快速开始

### 1. 配置本地环境

复制 `application-local.yml` 并修改其中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_lost_found
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 2. 初始化数据库

#### 方式一：使用MySQL命令行

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source lostandfound/src/main/resources/schema.sql

# 可选：导入测试数据
source lostandfound/src/main/resources/data-test.sql
```

#### 方式二：直接执行SQL文件

```bash
mysql -u root -p < lostandfound/src/main/resources/schema.sql
```

#### 验证数据库

```sql
USE campuslostandfound;
SHOW TABLES;
-- 应该看到7张表: user, item, item_image, item_tag, match_record, message, point_record
```

详细的数据库设计文档请参考：[docs/database-schema.md](docs/database-schema.md)

### 3. 编译项目

```bash
# Windows
.\mvnw.cmd clean compile

# Linux/Mac
./mvnw clean compile
```

编译成功后会看到：
```
[INFO] BUILD SUCCESS
[INFO] Compiling 16 source files
```

### 4. 运行应用

```bash
# Windows
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

或者使用环境变量：

```bash
# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="local"; .\mvnw.cmd spring-boot:run

# Linux/Mac
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

### 5. 运行测试

```bash
# Windows
.\mvnw.cmd clean verify

# Linux/Mac
./mvnw clean verify
```

## 配置说明

### 环境变量

生产环境建议使用环境变量配置敏感信息：

- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码
- `REDIS_HOST`: Redis主机地址
- `REDIS_PASSWORD`: Redis密码
- `JWT_SECRET`: JWT密钥（至少256位）
- `OSS_ENDPOINT`: 阿里云OSS端点
- `OSS_ACCESS_KEY_ID`: OSS访问密钥ID
- `OSS_ACCESS_KEY_SECRET`: OSS访问密钥
- `OSS_BUCKET_NAME`: OSS存储桶名称
- `BAIDU_AI_APP_ID`: 百度AI应用ID
- `BAIDU_AI_API_KEY`: 百度AI API密钥
- `BAIDU_AI_SECRET_KEY`: 百度AI密钥
- `AMAP_KEY`: 高德地图API密钥

### Profile配置

- `default`: 默认配置（使用环境变量）
- `local`: 本地开发配置（使用application-local.yml）

## API文档

启动应用后访问：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## 开发规范

### 项目结构说明

```
src/main/java/com/campus/lostandfound/
├── config/                          # 配置类
│   └── MyBatisPlusConfig.java      # ✅ MyBatis-Plus配置（分页、自动填充）
├── controller/                      # 控制器层（REST API）
├── service/                         # 服务层接口
│   └── impl/                        # 服务层实现
├── repository/                      # 数据访问层
│   ├── UserMapper.java             # ✅ 用户Mapper
│   ├── ItemMapper.java             # ✅ 物品Mapper
│   ├── ItemImageMapper.java        # ✅ 物品图片Mapper
│   ├── ItemTagMapper.java          # ✅ 物品标签Mapper
│   ├── MatchRecordMapper.java      # ✅ 匹配记录Mapper
│   ├── MessageMapper.java          # ✅ 消息Mapper
│   └── PointRecordMapper.java      # ✅ 积分记录Mapper
├── model/
│   ├── entity/                      # 实体类
│   │   ├── User.java               # ✅ 用户实体
│   │   ├── Item.java               # ✅ 物品实体（含软删除）
│   │   ├── ItemImage.java          # ✅ 物品图片实体
│   │   ├── ItemTag.java            # ✅ 物品标签实体
│   │   ├── MatchRecord.java        # ✅ 匹配记录实体
│   │   ├── Message.java            # ✅ 消息实体
│   │   └── PointRecord.java        # ✅ 积分记录实体
│   ├── dto/                         # 数据传输对象
│   └── vo/                          # 视图对象
├── common/                          # 公共组件
├── exception/                       # 异常处理
├── util/                            # 工具类
└── websocket/                       # WebSocket处理器
```

### 代码风格
- 使用4空格缩进
- 类名使用UpperCamelCase
- 方法和字段使用lowerCamelCase
- 常量使用UPPER_SNAKE_CASE

### REST API规范
- 使用名词表示资源
- 路径使用kebab-case
- 统一版本前缀：`/api/v1/`
- 使用DTO接收请求，VO返回响应

### 提交规范
使用Conventional Commits格式：
- `feat:` 新功能
- `fix:` 修复bug
- `docs:` 文档更新
- `test:` 测试相关
- `refactor:` 重构
- `chore:` 构建/工具相关

## 相关文档

- [数据库设计文档](docs/database-schema.md) - 详细的数据库表结构说明
- [数据库设置指南](docs/database-setup-guide.md) - 数据库安装和配置
- [Windows环境设置](docs/WINDOWS_SETUP.md) - Windows开发环境配置
- [快速开始数据库](docs/QUICK_START_DATABASE.md) - 数据库快速初始化

## 常见问题

### 编译错误

**问题**: `mvn: The term 'mvn' is not recognized`

**解决**: 使用Maven Wrapper
```bash
# Windows
.\mvnw.cmd clean compile

# Linux/Mac
./mvnw clean compile
```

### 数据库连接失败

**问题**: `Communications link failure`

**解决**:
1. 确认MySQL服务已启动
2. 检查 `application-local.yml` 中的数据库配置
3. 确认数据库 `campuslostandfound` 已创建
4. 验证用户名和密码正确

### 时区问题

**问题**: 时间字段保存后时区不正确

**解决**: 在数据库连接URL中添加时区参数
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslostandfound?serverTimezone=Asia/Shanghai
```

## 许可证

[待定]
