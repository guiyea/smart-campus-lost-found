<template>
  <div class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside class="sidebar" width="240px">
      <div class="logo-section">
        <el-icon class="logo-icon"><Setting /></el-icon>
        <span class="logo-text">管理后台</span>
      </div>
      
      <el-menu
        :default-active="activeIndex"
        class="sidebar-menu"
        @select="handleMenuSelect"
        :collapse="isCollapse"
        :collapse-transition="false"
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
          <el-menu-item index="/admin/statistics/daily">
            <el-icon><TrendCharts /></el-icon>
            <span>每日统计</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics/category">
            <el-icon><PieChart /></el-icon>
            <span>分类统计</span>
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
            <span>物品信息</span>
          </el-menu-item>
          <el-menu-item index="/admin/matches">
            <el-icon><Connection /></el-icon>
            <span>匹配记录</span>
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
          <el-menu-item index="/admin/users/banned">
            <el-icon><Lock /></el-icon>
            <span>封禁用户</span>
          </el-menu-item>
          <el-menu-item index="/admin/points">
            <el-icon><Medal /></el-icon>
            <span>积分管理</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 系统设置 -->
        <el-menu-item index="/admin/settings">
          <el-icon><Tools /></el-icon>
          <span>系统设置</span>
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
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>

          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="breadcrumb">
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
                {{ userStore.userInfo?.name?.charAt(0) }}
              </el-avatar>
              <span class="admin-name">{{ userStore.userInfo?.name }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="frontend">前台首页</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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
  TrendCharts,
  PieChart,
  Document,
  List,
  Connection,
  Warning,
  User,
  UserFilled,
  Lock,
  Medal,
  Tools,
  Fold,
  Expand,
  ArrowDown
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单项
const activeIndex = computed(() => route.path)

// 面包屑导航数据
const breadcrumbItems = computed(() => {
  const pathSegments = route.path.split('/').filter(Boolean)
  const items = [{ title: '首页', path: '/admin/dashboard' }]
  
  // 根据路径生成面包屑
  const routeMap: Record<string, string> = {
    'admin': '管理后台',
    'dashboard': '数据概览',
    'statistics': '数据统计',
    'daily': '每日统计',
    'category': '分类统计',
    'items': '物品信息',
    'matches': '匹配记录',
    'reports': '举报处理',
    'users': '用户管理',
    'banned': '封禁用户',
    'points': '积分管理',
    'settings': '系统设置'
  }
  
  let currentPath = ''
  for (let i = 1; i < pathSegments.length; i++) {
    const segment = pathSegments[i]
    currentPath += `/${pathSegments.slice(0, i + 1).join('/')}`
    
    if (segment && routeMap[segment]) {
      items.push({
        title: routeMap[segment],
        path: currentPath
      })
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
      router.push('/user/profile')
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
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/auth/login')
  } catch (error) {
    ElMessage.error('退出登录失败')
  }
}

// 监听路由变化，自动展开对应的子菜单
watch(
  () => route.path,
  () => {
    // 根据路径自动展开对应的子菜单
    // 这里可以添加自动展开逻辑
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  display: flex;
}

.sidebar {
  background: #304156;
  overflow: hidden;
  
  .logo-section {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #2b3a4b;
    color: #fff;
    
    .logo-icon {
      font-size: 24px;
      margin-right: 8px;
    }
    
    .logo-text {
      font-size: 16px;
      font-weight: bold;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    background: #304156;
    
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: #bfcbd9;
      
      &:hover {
        background-color: #263445;
        color: #fff;
      }
    }
    
    :deep(.el-menu-item.is-active) {
      background-color: #409eff;
      color: #fff;
    }
    
    :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
      color: #409eff;
    }
    
    :deep(.el-menu--inline) {
      background: #1f2d3d;
      
      .el-menu-item {
        &:hover {
          background-color: #001528;
        }
        
        &.is-active {
          background-color: #409eff;
        }
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      margin-right: 20px;
      font-size: 18px;
    }
    
    .breadcrumb {
      :deep(.el-breadcrumb__item) {
        .el-breadcrumb__inner {
          color: #606266;
          
          &:hover {
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
      padding: 8px;
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
}

// 折叠状态下的样式调整
.sidebar.el-aside--collapse {
  .logo-section {
    .logo-text {
      display: none;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .admin-layout {
    .sidebar {
      position: fixed;
      z-index: 1000;
      height: 100vh;
      
      &:not(.el-aside--collapse) {
        width: 240px !important;
      }
    }
    
    .main-container {
      margin-left: 0;
    }
  }
}
</style>
