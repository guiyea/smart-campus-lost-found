# 智能校园失物招领平台

<div align="center">

**基于 AI 图像识别和智能匹配的现代化校园失物招领系统**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-green.svg)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

[功能特性](#功能特性) • [快速开始](#快速开始) • [技术架构](#技术架构) • [文档](#文档) • [贡献指南](#贡献指南)

</div>

---

## 📖 项目简介

智能校园失物招领平台是一个集成了 AI 图像识别、智能匹配算法、LBS 地理服务等先进技术的现代化失物招领管理系统。通过自动化和智能化的方式，为校园师生提供高效便捷的失物招领服务。

### 🎯 核心亮点

- 🤖 **AI 图像识别** - 基于阿里云视觉智能，自动识别物品类别和特征
- 🎯 **智能匹配算法** - 多维度相似度计算，精准推荐匹配物品
- 📍 **LBS 地理服务** - 基于高德地图，支持地理位置查询和附近搜索
- 💬 **实时消息推送** - WebSocket 实时通知，第一时间获取匹配信息
- 🏆 **积分激励系统** - 完善的积分机制，鼓励用户积极参与
- 📊 **数据统计分析** - 可视化数据大屏，全面掌握运营情况

## 🚀 功能特性

### 用户端功能

- ✅ 用户注册/登录（学号/手机号）
- ✅ 发布失物/招领信息
- ✅ AI 自动识别物品类别
- ✅ 多图上传（最多 9 张）
- ✅ 关键词搜索和多条件筛选
- ✅ 地理位置选择和附近搜索
- ✅ 智能匹配推荐（Top 10）
- ✅ 匹配确认和反馈
- ✅ 实时消息通知
- ✅ 积分明细和排行榜
- ✅ 个人中心和物品管理

### 管理端功能

- ✅ 数据统计仪表盘
- ✅ 物品审核管理
- ✅ 用户管理（封禁/解封）
- ✅ 数据导出（Excel 报表）
- ✅ 系统配置管理

## 🏗️ 技术架构

### 技术栈

**后端**
- Spring Boot 3.4.1 + Java 21
- MySQL 8.0 + MyBatis-Plus 3.5.5
- Redis 7.x（缓存 + 消息队列）
- Spring Security + JWT
- 阿里云 OSS（文件存储）
- 阿里云视觉智能（图像识别）
- 高德地图 API（LBS 服务）

**前端**
- Vue 3.x + TypeScript 5.x
- Vite 5.x + Element Plus 2.x
- Pinia 2.x（状态管理）
- Vue Router 4.x
- Axios + WebSocket

**测试**
- JUnit 5 + jqwik（属性测试）
- Mockito + Spring Boot Test

**部署**
- Docker + Docker Compose
- Nginx（反向代理）

### 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                    前端层 (Vue 3)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐│
│  │ 用户界面 │  │ 管理后台 │  │ 地图组件 │  │ 消息推送 ││
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘│
└─────────────────────────────────────────────────────────┘
                          ↓ HTTP/WebSocket
┌─────────────────────────────────────────────────────────┐
│                   API 网关 / Nginx                       │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                 应用层 (Spring Boot)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐│
│  │ 认证服务 │  │ 物品服务 │  │ 匹配服务 │  │ 消息服务 ││
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘│
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                    数据层 & 外部服务                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐│
│  │  MySQL   │  │  Redis   │  │ 阿里云OSS│  │ 阿里云AI ││
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘│
└─────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.x+
- **Maven**: 3.8+ (或使用项目自带的 Maven Wrapper)

### 一键启动（推荐）

**Windows 用户**：

```cmd
# 1. 初始化数据库
cd lostandfound\scripts
init-database.bat

# 2. 启动后端
cd ..
mvnw.cmd spring-boot:run

# 3. 启动前端（新窗口）
cd ..\web
npm install
npm run dev
```

**Linux/Mac 用户**：

```bash
# 1. 初始化数据库
cd lostandfound
mysql -u root -p < src/main/resources/schema.sql

# 2. 配置环境
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# 编辑 application-local.yml 填入配置

# 3. 启动后端
./mvnw spring-boot:run

# 4. 启动前端（新终端）
cd ../web
npm install
npm run dev
```

### 访问应用

- **前端界面**: http://localhost:5173
- **后端 API**: http://localhost:8080/api/v1
- **API 文档**: http://localhost:8080/swagger-ui.html

### 测试账号

如果导入了测试数据，可以使用：

| 学号 | 密码 | 角色 |
|------|------|------|
| 2021001 | password123 | 普通用户 |
| 2021002 | password123 | 普通用户 |
| ADMIN001 | password123 | 管理员 |

## 📁 项目结构

```
smart-campus-lost-found/
├── lostandfound/              # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java 源代码
│   │   │   └── resources/     # 配置文件
│   │   └── test/              # 测试代码
│   ├── docs/                  # 后端文档
│   ├── scripts/               # 脚本文件
│   ├── deploy/                # 部署配置
│   └── pom.xml                # Maven 配置
├── web/                       # 前端项目
│   ├── src/
│   │   ├── api/               # API 接口
│   │   ├── components/        # 组件
│   │   ├── views/             # 页面
│   │   ├── stores/            # 状态管理
│   │   └── utils/             # 工具函数
│   ├── public/                # 静态资源
│   └── package.json           # 依赖配置
├── .kiro/                     # 项目规范文档
│   └── specs/
│       └── smart-campus-lost-found/
│           ├── requirements.md  # 需求文档
│           ├── design.md        # 设计文档
│           └── tasks.md         # 任务清单
└── README.md                  # 本文件
```

## 📚 文档

### 核心文档

- [项目需求文档](.kiro/specs/smart-campus-lost-found/requirements.md)
- [系统设计文档](.kiro/specs/smart-campus-lost-found/design.md)
- [任务清单](.kiro/specs/smart-campus-lost-found/tasks.md)

### 开发文档

- [后端 README](lostandfound/README.md)
- [前端 README](web/README.md)
- [文档中心](lostandfound/docs/README.md)
- [测试文档](lostandfound/docs/testing/README.md)

### 配置指南

- [快速开始指南](lostandfound/快速开始.md)
- [Windows 环境设置](lostandfound/docs/WINDOWS_SETUP.md)
- [数据库设置指南](lostandfound/docs/database-setup-guide.md)
- [OSS 集成指南](lostandfound/docs/oss-integration-guide.md)

### 部署文档

- [Docker 部署指南](lostandfound/deploy/DEPLOYMENT.md)
- [环境变量配置](lostandfound/deploy/.env.example)

## 🧪 测试

### 运行测试

```bash
# 后端测试
cd lostandfound
.\mvnw.cmd clean verify

# 前端测试（计划中）
cd web
npm run test
```

### 测试覆盖

- ✅ 单元测试：68 个
- ✅ 属性测试：4 个
- ✅ 集成测试：7 个
- ✅ 手动测试：完整测试指南

详细测试文档请查看 [测试文档中心](lostandfound/docs/testing/README.md)

## 🚀 部署

### Docker 部署（推荐）

```bash
cd lostandfound/deploy

# 配置环境变量
cp .env.example .env
# 编辑 .env 文件

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

详细部署说明请查看 [部署文档](lostandfound/deploy/DEPLOYMENT.md)

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` 修复 Bug
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建/工具相关

### 代码规范

- **后端**: 遵循 Java 编码规范，使用 4 空格缩进
- **前端**: 遵循 Vue 3 风格指南，使用 ESLint + Prettier
- **提交**: 使用 Conventional Commits 规范
- **测试**: 新功能必须包含单元测试

## 📊 开发进度

| 模块 | 状态 | 完成度 |
|------|------|--------|
| 基础架构 | ✅ 完成 | 100% |
| 用户认证 | ✅ 完成 | 100% |
| 文件上传 | ✅ 完成 | 100% |
| 物品管理 | ✅ 完成 | 100% |
| AI 识别 | ✅ 完成 | 100% |
| LBS 服务 | ✅ 完成 | 100% |
| 搜索筛选 | ✅ 完成 | 100% |
| 智能匹配 | ✅ 完成 | 100% |
| 消息通知 | ✅ 完成 | 100% |
| 积分系统 | ✅ 完成 | 100% |
| 管理后台 | ✅ 完成 | 100% |
| 系统优化 | ✅ 完成 | 100% |
| 前端界面 | ✅ 完成 | 100% |
| 部署配置 | ✅ 完成 | 100% |

## ❓ 常见问题

### Q: 如何快速启动项目？

查看 [快速开始指南](lostandfound/快速开始.md)

### Q: Windows 环境如何配置？

查看 [Windows 环境设置](lostandfound/docs/WINDOWS_SETUP.md)

### Q: 数据库连接失败？

1. 确认 MySQL 服务已启动
2. 检查配置文件中的数据库连接信息
3. 确认数据库已创建

### Q: 如何配置阿里云服务？

查看 [OSS 集成指南](lostandfound/docs/oss-integration-guide.md)

更多问题请查看各模块的 README 文档。

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢以下开源项目和服务：

- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Element Plus](https://element-plus.org/) - UI 组件库
- [MyBatis-Plus](https://baomidou.com/) - ORM 框架
- [阿里云](https://www.aliyun.com/) - 云服务提供商
- [高德地图](https://lbs.amap.com/) - 地图服务

## 📮 联系方式

- **项目地址**: [GitHub Repository]
- **问题反馈**: [Issues]
- **文档**: [Wiki]
- **邮箱**: [Email]

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star！**

Made with ❤️ by Smart Campus Team

</div>
