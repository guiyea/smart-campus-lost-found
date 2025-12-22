# 智能校园失物招领平台 - 后端服务

> 💡 **Windows 用户快速开始**：查看 [Windows 环境设置指南](docs/WINDOWS_SETUP.md)

## 项目状态

✅ **已完成**
- 项目初始化与依赖配置
- 数据库表结构设计与创建
- MyBatis-Plus基础代码生成（Entity + Mapper）
- 分页插件配置
- 自动填充时间字段配置
- 统一响应与异常处理
- **用户认证模块（JWT + Spring Security）** ⭐ 新完成
  - 用户注册功能（学号/手机号唯一性验证）
  - 用户登录功能（密码验证、令牌生成）
  - JWT令牌认证（访问令牌 + 刷新令牌）
  - 登录失败5次锁定机制（15分钟）
  - 每日首次登录积分奖励（+2积分）
  - 用户信息管理（查询、更新）
  - 全局异常处理器
  - **测试覆盖**: 42个单元测试全部通过 ✅

🚧 **进行中**
- 文件上传模块（阿里云OSS集成）

📋 **待开发**
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
│   │   │   │   ├── MyBatisPlusConfig.java    # MyBatis-Plus配置
│   │   │   │   ├── SecurityConfig.java       # ✅ Spring Security配置
│   │   │   │   └── JwtAuthenticationFilter.java # ✅ JWT认证过滤器
│   │   │   ├── controller/          # ✅ 控制器层（REST API接口）
│   │   │   │   ├── AuthController.java       # ✅ 认证接口（注册/登录）
│   │   │   │   └── UserController.java       # ✅ 用户信息接口
│   │   │   ├── service/             # ✅ 服务层接口
│   │   │   │   ├── AuthService.java          # ✅ 认证服务接口
│   │   │   │   ├── UserService.java          # ✅ 用户服务接口
│   │   │   │   └── impl/            # ✅ 服务层实现
│   │   │   │       ├── AuthServiceImpl.java  # ✅ 认证服务实现
│   │   │   │       └── UserServiceImpl.java  # ✅ 用户服务实现
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
│   │   │   │   ├── dto/             # ✅ 数据传输对象（请求参数）
│   │   │   │   │   ├── RegisterDTO.java      # ✅ 注册请求
│   │   │   │   │   ├── LoginDTO.java         # ✅ 登录请求
│   │   │   │   │   └── UpdateProfileDTO.java # ✅ 更新用户信息请求
│   │   │   │   └── vo/              # ✅ 视图对象（响应数据）
│   │   │   │       ├── UserVO.java            # ✅ 用户信息响应
│   │   │   │       └── TokenVO.java           # ✅ 令牌响应
│   │   │   ├── common/              # ✅ 公共组件
│   │   │   │   ├── Result.java               # ✅ 统一响应格式
│   │   │   │   ├── PageResult.java           # ✅ 分页响应
│   │   │   │   └── ResultCode.java           # ✅ 响应状态码枚举
│   │   │   ├── exception/           # ✅ 异常处理
│   │   │   │   ├── GlobalExceptionHandler.java # ✅ 全局异常处理器
│   │   │   │   ├── BusinessException.java      # ✅ 业务异常
│   │   │   │   ├── UnauthorizedException.java  # ✅ 未认证异常
│   │   │   │   ├── ForbiddenException.java     # ✅ 无权限异常
│   │   │   │   ├── NotFoundException.java      # ✅ 资源不存在异常
│   │   │   │   ├── ValidationException.java    # ✅ 参数验证异常
│   │   │   │   └── RateLimitException.java     # ✅ 请求频率超限异常
│   │   │   ├── util/                # ✅ 工具类
│   │   │   │   ├── JwtUtil.java              # ✅ JWT工具类
│   │   │   │   └── RedisUtil.java            # ✅ Redis工具类
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
│   └── test/                        # ✅ 测试代码
│       └── java/com/campus/lostandfound/
│           ├── LostandfoundApplicationTests.java # ✅ 应用启动测试
│           ├── config/
│           │   └── JwtAuthenticationFilterTest.java # ✅ JWT过滤器测试
│           ├── exception/
│           │   └── GlobalExceptionHandlerTest.java  # ✅ 异常处理器测试
│           ├── service/
│           │   └── AuthServiceTest.java             # ✅ 认证服务测试
│           └── util/
│               └── JwtUtilTest.java                 # ✅ JWT工具类测试
├── test-auth-endpoints.md           # ✅ 认证接口手动测试指南
├── checkpoint-8-verification-report.md # ✅ Checkpoint 8验证报告
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
    password: your_redis_password  # 如果Redis设置了密码

jwt:
  secret: your-256-bit-secret-key-here-at-least-32-characters-long
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

### 3. 启动Redis服务

确保Redis服务已启动（用于登录失败计数和缓存）：

```bash
# Windows: 启动Redis服务
redis-server

# Linux/Mac
sudo systemctl start redis
# 或
redis-server
```

### 4. 编译项目

```bash
# Windows
.\mvnw.cmd clean compile

# Linux/Mac
./mvnw clean compile
```

编译成功后会看到：
```
[INFO] BUILD SUCCESS
[INFO] Compiling 41 source files
```

### 5. 运行测试

```bash
# Windows
.\mvnw.cmd clean verify

# Linux/Mac
./mvnw clean verify
```

测试结果：
```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 6. 运行应用

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

应用启动成功后，访问：http://localhost:8080

### 7. 测试API接口

参考 [test-auth-endpoints.md](test-auth-endpoints.md) 进行手动测试。

#### 快速测试示例

**注册用户**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"studentId\":\"2021001\",\"name\":\"张三\",\"phone\":\"13800138000\",\"password\":\"123456\"}"
```

**用户登录**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"studentId\":\"2021001\",\"password\":\"123456\"}"
```

**获取用户信息**（需要替换YOUR_TOKEN）
```bash
curl -X GET http://localhost:8080/api/v1/users/me ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
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

### 已实现的API接口

#### 认证接口 (`/api/v1/auth`)

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| POST | `/register` | 用户注册 | ✅ |
| POST | `/login` | 用户登录 | ✅ |
| POST | `/refresh` | 刷新令牌 | ✅ |

#### 用户接口 (`/api/v1/users`)

| 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|
| GET | `/me` | 获取当前用户信息 | 需要 | ✅ |
| PUT | `/me` | 更新当前用户信息 | 需要 | ✅ |
| GET | `/` | 查询用户列表（管理员） | 需要 | ✅ |

### Swagger文档

启动应用后访问：
- Swagger UI: http://localhost:8080/swagger-ui.html （待配置）
- API Docs: http://localhost:8080/v3/api-docs （待配置）

### 手动测试

详细的测试指南请参考：[test-auth-endpoints.md](test-auth-endpoints.md)

## 开发规范

## 开发规范

### 项目结构说明

```
src/main/java/com/campus/lostandfound/
├── config/                          # 配置类
│   ├── MyBatisPlusConfig.java      # ✅ MyBatis-Plus配置（分页、自动填充）
│   ├── SecurityConfig.java         # ✅ Spring Security配置
│   └── JwtAuthenticationFilter.java # ✅ JWT认证过滤器
├── controller/                      # ✅ 控制器层（REST API）
│   ├── AuthController.java         # ✅ 认证接口
│   └── UserController.java         # ✅ 用户接口
├── service/                         # ✅ 服务层接口
│   ├── AuthService.java            # ✅ 认证服务
│   ├── UserService.java            # ✅ 用户服务
│   └── impl/                        # ✅ 服务层实现
│       ├── AuthServiceImpl.java    # ✅ 认证服务实现
│       └── UserServiceImpl.java    # ✅ 用户服务实现
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
│   ├── dto/                         # ✅ 数据传输对象
│   │   ├── RegisterDTO.java        # ✅ 注册请求
│   │   ├── LoginDTO.java           # ✅ 登录请求
│   │   └── UpdateProfileDTO.java   # ✅ 更新用户信息
│   └── vo/                          # ✅ 视图对象
│       ├── UserVO.java             # ✅ 用户信息响应
│       └── TokenVO.java            # ✅ 令牌响应
├── common/                          # ✅ 公共组件
│   ├── Result.java                 # ✅ 统一响应格式
│   ├── PageResult.java             # ✅ 分页响应
│   └── ResultCode.java             # ✅ 响应状态码
├── exception/                       # ✅ 异常处理
│   ├── GlobalExceptionHandler.java # ✅ 全局异常处理器
│   ├── BusinessException.java      # ✅ 业务异常
│   ├── UnauthorizedException.java  # ✅ 未认证异常
│   ├── ForbiddenException.java     # ✅ 无权限异常
│   ├── NotFoundException.java      # ✅ 资源不存在异常
│   ├── ValidationException.java    # ✅ 参数验证异常
│   └── RateLimitException.java     # ✅ 请求频率超限异常
├── util/                            # ✅ 工具类
│   ├── JwtUtil.java                # ✅ JWT工具类
│   └── RedisUtil.java              # ✅ Redis工具类
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
- [认证接口测试指南](test-auth-endpoints.md) - 用户认证模块手动测试
- [Checkpoint 8验证报告](checkpoint-8-verification-report.md) - 用户认证模块验证结果

## 测试报告

### 最新测试结果（Checkpoint 8）

**日期**: 2025-12-22  
**状态**: ✅ 通过

```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 31.948 s
```

**测试覆盖**:
- ✅ JWT认证过滤器测试 (7个测试)
- ✅ 全局异常处理器测试 (10个测试)
- ✅ 应用启动测试 (1个测试)
- ✅ 认证服务测试 (14个测试)
- ✅ JWT工具类测试 (10个测试)

详细报告请查看：[checkpoint-8-verification-report.md](checkpoint-8-verification-report.md)

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

### Redis连接失败

**问题**: `Unable to connect to Redis`

**解决**:
1. 确认Redis服务已启动
2. 检查 `application-local.yml` 中的Redis配置
3. 如果Redis设置了密码，确保配置正确
4. 测试Redis连接：`redis-cli ping`（应返回PONG）

### JWT令牌验证失败

**问题**: `401 Unauthorized` 或 `Invalid JWT token`

**解决**:
1. 确认请求头包含 `Authorization: Bearer <token>`
2. 检查令牌是否过期（访问令牌2小时有效）
3. 使用刷新令牌获取新的访问令牌
4. 确认JWT密钥配置正确（至少32字符）

### 登录失败账户锁定

**问题**: 测试时账户被锁定

**解决**:
1. 等待15分钟后自动解锁
2. 或手动清除Redis中的锁定记录：
   ```bash
   redis-cli
   > DEL login:fail:2021001
   ```

### 时区问题

**问题**: 时间字段保存后时区不正确

**解决**: 在数据库连接URL中添加时区参数
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslostandfound?serverTimezone=Asia/Shanghai
```

### 测试失败

**问题**: 运行测试时出现失败

**解决**:
1. 确保数据库和Redis服务正常运行
2. 检查测试数据是否正确初始化
3. 查看具体的错误日志
4. 运行 `.\mvnw.cmd clean test` 清理后重新测试

## 许可证

[待定]
