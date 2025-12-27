<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo">
          <el-icon class="logo-icon"><Location /></el-icon>
          <span class="logo-text">校园失物招领</span>
        </div>

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索失物信息..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>

        <!-- 导航菜单 -->
        <el-menu
          :default-active="activeIndex"
          class="nav-menu"
          mode="horizontal"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/items">失物信息</el-menu-item>
          <el-menu-item index="/publish">发布信息</el-menu-item>
          <el-menu-item index="/points">积分中心</el-menu-item>
        </el-menu>

        <!-- 用户信息/登录按钮 -->
        <div class="user-section">
          <template v-if="userStore.isLoggedIn">
            <!-- 消息通知 -->
            <MessageBadge 
              ref="messageBadgeRef"
              :poll-interval="60000"
              @update:count="handleUnreadCountUpdate"
            />

            <!-- 用户下拉菜单 -->
            <el-dropdown @command="handleUserCommand">
              <span class="user-info">
                <el-avatar :src="userStore.userInfo?.avatar" :size="32">
                  {{ userStore.userInfo?.name?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.name }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="messages">我的消息</el-dropdown-item>
                  <el-dropdown-item command="items">我的发布</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 底部版权信息 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 智能校园失物招领平台. All rights reserved.</p>
        <p>让每一件失物都能找到回家的路</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { 
  Location, 
  Search, 
  ArrowDown 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MessageBadge from '@/components/message/MessageBadge.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')

// 未读消息数量（用于其他地方显示）
const unreadCount = ref(0)

// 当前激活的菜单项
const activeIndex = computed(() => route.path)

// 处理未读数量更新
const handleUnreadCountUpdate = (count: number) => {
  unreadCount.value = count
}

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/items',
      query: { keyword: searchKeyword.value.trim() }
    })
  }
}

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  router.push(index)
}

// 处理用户下拉菜单命令
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/my/profile')
      break
    case 'messages':
      router.push('/messages')
      break
    case 'items':
      router.push('/my/items')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('退出登录失败')
  }
}

onMounted(() => {
  // MessageBadge 组件会自动获取未读消息数量
})
</script>

<style scoped lang="scss">
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
  height: 60px;
  
  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 20px;
  }
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 40px;
  
  .logo-icon {
    font-size: 24px;
    color: #409eff;
    margin-right: 8px;
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
  }
}

.search-box {
  flex: 1;
  max-width: 400px;
  margin-right: 40px;
  
  .search-input {
    width: 100%;
  }
}

.nav-menu {
  border-bottom: none;
  margin-right: 40px;
  
  :deep(.el-menu-item) {
    border-bottom: none;
    
    &:hover {
      background-color: #ecf5ff;
    }
    
    &.is-active {
      color: #409eff;
      border-bottom: 2px solid #409eff;
    }
  }
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .message-badge {
    .el-button {
      font-size: 18px;
    }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 8px;
    border-radius: 4px;
    transition: background-color 0.3s;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    .username {
      margin: 0 8px;
      font-size: 14px;
      color: #606266;
    }
  }
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  
  :deep(.el-main) {
    max-width: 1200px;
    margin: 0 auto;
  }
}

.footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 20px 0;
  
  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    text-align: center;
    color: #909399;
    
    p {
      margin: 4px 0;
      font-size: 14px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .logo {
    margin-right: 20px;
    
    .logo-text {
      display: none;
    }
  }
  
  .search-box {
    max-width: 200px;
    margin-right: 20px;
  }
  
  .nav-menu {
    margin-right: 20px;
    
    :deep(.el-menu-item) {
      padding: 0 10px;
      font-size: 14px;
    }
  }
  
  .user-section {
    gap: 8px;
    
    .username {
      display: none;
    }
  }
}
</style>
