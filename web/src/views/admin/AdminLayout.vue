<template>
  <div class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside class="sidebar" :width="isCollapse ? '64px' : '240px'">
      <div class="logo-section">
        <el-icon class="logo-icon"><Setting /></el-icon>
        <span v-if="!isCollapse" class="logo-text">管理后台</span>
      </div>
      
      <el-menu
        :default-active="activeIndex"
        class="sidebar-menu"
        @select="handleMenuSelect"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <!-- 数据统计 -->
        <el-sub-menu index="statistics">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </template>
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 内容管理 -->
        <el-sub-menu index="content">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/admin/items">
            <el-icon><List /></el-icon>
            <span>物品列表</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <el-icon><Warning /></el-icon>
            <span>举报处理</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 用户管理 -->
        <el-sub-menu index="users">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon>
            <span>用户列表</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 数据导出 -->
        <el-menu-item index="/admin/export">
          <el-icon><Download /></el-icon>
          <span>数据导出</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container class="main-container">
      <!-- 顶部面包屑导航 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 折叠按钮 -->
          <el-button
            text
            @click="toggleCollapse"
            class="collapse-btn"
          >
            <el-icon :size="20"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>

          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">
              管理后台
            </el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="item in breadcrumbItems"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 管理员信息 -->
          <el-dropdown @command="handleUserCommand">
            <span class="admin-info">
              <el-avatar :src="userStore.userInfo?.avatar" :size="32">
                {{ userStore.userInfo?.name?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="admin-name">{{ userStore.userInfo?.name || '管理员' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="frontend">
                  <el-icon><HomeFilled /></el-icon>
                  前台首页
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import {
  Setting,
  DataAnalysis,
  Odometer,
  Document,
  List,
  Warning,
  User,
  UserFilled,
  Download,
  Fold,
  Expand,
  ArrowDown,
  HomeFilled,
  SwitchButton
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单项
const activeIndex = computed(() => route.path)

// 路由名称映射
const routeNameMap: Record<string, string> = {
  'dashboard': '数据概览',
  'items': '物品列表',
  'reports': '举报处理',
  'users': '用户列表',
  'export': '数据导出'
}

// 面包屑导航数据
const breadcrumbItems = computed(() => {
  const pathSegments = route.path.split('/').filter(Boolean)
  const items: Array<{ title: string; path: string }> = []
  
  // 跳过 'admin' 前缀
  for (let i = 1; i < pathSegments.length; i++) {
    const segment = pathSegments[i]
    if (segment) {
      const title = routeNameMap[segment]
      
      if (title) {
        items.push({
          title,
          path: '/' + pathSegments.slice(0, i + 1).join('/')
        })
      }
    }
  }
  
  return items
})

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
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
    case 'frontend':
      router.push('/')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch {
    // 用户取消操作
  }
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    // 可以在这里添加路由变化时的逻辑
  },
  { immediate: true }
)
</script>


<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.sidebar {
  background: #304156;
  overflow: hidden;
  transition: width 0.3s;
  flex-shrink: 0;
  
  .logo-section {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #2b3a4b;
    color: #fff;
    
    .logo-icon {
      font-size: 24px;
    }
    
    .logo-text {
      font-size: 16px;
      font-weight: bold;
      margin-left: 8px;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    height: calc(100vh - 60px);
    overflow-y: auto;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 3px;
    }
    
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      &:hover {
        background-color: #263445 !important;
      }
    }
    
    :deep(.el-menu-item.is-active) {
      background-color: #409eff !important;
      color: #fff !important;
    }
    
    :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
      color: #409eff !important;
    }
    
    :deep(.el-menu--inline) {
      background: #1f2d3d !important;
      
      .el-menu-item {
        background: #1f2d3d !important;
        
        &:hover {
          background-color: #001528 !important;
        }
        
        &.is-active {
          background-color: #409eff !important;
        }
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  flex-shrink: 0;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      margin-right: 16px;
      padding: 8px;
      
      &:hover {
        background-color: #f5f7fa;
      }
    }
    
    .breadcrumb {
      :deep(.el-breadcrumb__item) {
        .el-breadcrumb__inner {
          color: #606266;
          
          &:hover {
            color: #409eff;
          }
          
          &.is-link:hover {
            color: #409eff;
          }
        }
        
        &:last-child .el-breadcrumb__inner {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }
  
  .header-right {
    .admin-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 4px;
      transition: background-color 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .admin-name {
        margin: 0 8px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

// 响应式设计
@media (max-width: 768px) {
  .admin-layout {
    .sidebar {
      position: fixed;
      z-index: 1000;
      height: 100vh;
    }
    
    .main-container {
      margin-left: 0;
    }
    
    .header {
      .admin-name {
        display: none;
      }
    }
  }
}
</style>
