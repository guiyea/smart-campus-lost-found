# 智能校园失物招领平台 - 前端

基于 Vue 3 + TypeScript + Vite + Element Plus 构建的现代化前端应用。

## 技术栈

- **框架**: Vue 3.x (Composition API + `<script setup>`)
- **语言**: TypeScript 5.x
- **构建工具**: Vite 5.x
- **UI 框架**: Element Plus 2.x
- **状态管理**: Pinia 2.x
- **路由**: Vue Router 4.x
- **HTTP 客户端**: Axios 1.x
- **地图**: 高德地图 JS API 2.0
- **代码规范**: ESLint + Prettier

## 项目结构

```
web/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口定义
│   │   ├── auth.ts        # 认证接口
│   │   ├── user.ts        # 用户接口
│   │   ├── item.ts        # 物品接口
│   │   ├── match.ts       # 匹配接口
│   │   ├── message.ts     # 消息接口
│   │   ├── point.ts       # 积分接口
│   │   └── admin.ts       # 管理接口
│   ├── assets/            # 资源文件
│   ├── components/        # 组件
│   │   ├── item/          # 物品相关组件
│   │   ├── layout/        # 布局组件
│   │   ├── map/           # 地图组件
│   │   ├── match/         # 匹配组件
│   │   └── message/       # 消息组件
│   ├── config/            # 配置文件
│   ├── router/            # 路由配置
│   ├── stores/            # Pinia 状态管理
│   │   ├── user.ts        # 用户状态
│   │   └── app.ts         # 应用状态
│   ├── styles/            # 全局样式
│   ├── types/             # TypeScript 类型定义
│   ├── utils/             # 工具函数
│   │   ├── request.ts     # HTTP 请求封装
│   │   └── websocket.ts   # WebSocket 封装
│   ├── views/             # 页面组件
│   │   ├── auth/          # 认证页面
│   │   ├── items/         # 物品页面
│   │   ├── messages/      # 消息页面
│   │   ├── user/          # 用户页面
│   │   ├── admin/         # 管理后台
│   │   └── Home.vue       # 首页
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── .env                   # 环境变量（开发）
├── .env.production        # 环境变量（生产）
├── index.html             # HTML 模板
├── package.json           # 依赖配置
├── tsconfig.json          # TypeScript 配置
└── vite.config.ts         # Vite 配置
```

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 配置环境变量

编辑 `.env` 文件：

```env
# API 基础地址
VITE_API_BASE_URL=http://localhost:8080/api/v1

# WebSocket 地址
VITE_WS_URL=ws://localhost:8080/ws

# 高德地图 Key
VITE_AMAP_KEY=your_amap_key
```

### 3. 启动开发服务器

```bash
npm run dev
```

应用将在 http://localhost:5173 启动

### 4. 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist/` 目录

### 5. 预览生产构建

```bash
npm run preview
```

## 开发指南

### 组件开发

使用 Vue 3 Composition API 和 `<script setup>` 语法：

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Item } from '@/types'

const items = ref<Item[]>([])
const itemCount = computed(() => items.value.length)

const fetchItems = async () => {
  // API 调用
}
</script>

<template>
  <div>
    <h1>物品列表 ({{ itemCount }})</h1>
    <!-- 组件内容 -->
  </div>
</template>

<style scoped>
/* 组件样式 */
</style>
```

### API 调用

使用封装的 request 工具：

```typescript
import request from '@/utils/request'

// GET 请求
const getItems = () => {
  return request.get('/items')
}

// POST 请求
const createItem = (data: ItemDTO) => {
  return request.post('/items', data)
}

// 带认证的请求（自动添加 token）
const getProfile = () => {
  return request.get('/users/me')
}
```

### 状态管理

使用 Pinia 进行状态管理：

```typescript
// stores/user.ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  
  actions: {
    async login(credentials) {
      // 登录逻辑
    },
    
    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
    }
  }
})
```

### 路由配置

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/items',
      component: () => import('@/views/items/ItemList.vue')
    },
    {
      path: '/profile',
      component: () => import('@/views/user/Profile.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

### WebSocket 使用

```typescript
import { useWebSocket } from '@/utils/websocket'

const { connect, disconnect, send, onMessage } = useWebSocket()

// 连接
connect()

// 监听消息
onMessage((data) => {
  console.log('收到消息:', data)
})

// 发送消息
send({ type: 'chat', content: 'Hello' })

// 断开连接
disconnect()
```

## 功能模块

### 用户认证
- 用户注册/登录
- JWT 令牌管理
- 自动刷新令牌
- 路由守卫

### 物品管理
- 发布失物/招领
- 物品列表展示
- 物品详情查看
- 物品编辑/删除
- 图片上传（支持多图）
- 地图选点

### 搜索筛选
- 关键词搜索
- 多条件筛选
- 地理范围筛选
- 排序功能

### 智能匹配
- 匹配推荐列表
- 匹配详情查看
- 匹配确认
- 匹配反馈

### 消息通知
- 消息列表
- 实时消息推送
- 消息已读/未读
- 消息类型分类

### 积分系统
- 积分明细
- 积分排行榜
- 积分规则说明

### 管理后台
- 数据统计
- 物品审核
- 用户管理
- 数据导出

## 代码规范

### 命名规范

- **组件**: PascalCase (如 `ItemCard.vue`)
- **文件夹**: kebab-case (如 `item-list/`)
- **变量/函数**: camelCase (如 `getUserInfo`)
- **常量**: UPPER_SNAKE_CASE (如 `API_BASE_URL`)
- **类型/接口**: PascalCase (如 `UserInfo`)

### 提交规范

遵循 Conventional Commits：

```
feat: 添加物品搜索功能
fix: 修复登录页面样式问题
docs: 更新 README 文档
style: 格式化代码
refactor: 重构用户状态管理
test: 添加单元测试
chore: 更新依赖版本
```

## 常见问题

### Q: npm install 失败？

```bash
# 清除缓存
npm cache clean --force

# 使用国内镜像
npm config set registry https://registry.npmmirror.com

# 重新安装
npm install
```

### Q: 开发服务器启动失败？

1. 检查端口 5173 是否被占用
2. 检查 Node.js 版本（需要 18+）
3. 删除 `node_modules` 和 `package-lock.json` 重新安装

### Q: API 请求失败？

1. 确认后端服务已启动
2. 检查 `.env` 中的 API 地址
3. 检查浏览器控制台的 CORS 错误
4. 确认 token 是否有效

### Q: 地图不显示？

1. 检查高德地图 Key 是否配置
2. 确认 Key 是否有效
3. 检查浏览器控制台错误信息

## 性能优化

- 路由懒加载
- 组件按需导入
- 图片懒加载
- 虚拟滚动（长列表）
- 请求防抖/节流
- 缓存策略

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 相关链接

- [Vue 3 文档](https://vuejs.org/)
- [Vite 文档](https://vitejs.dev/)
- [Element Plus 文档](https://element-plus.org/)
- [Pinia 文档](https://pinia.vuejs.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)

## 许可证

MIT License



