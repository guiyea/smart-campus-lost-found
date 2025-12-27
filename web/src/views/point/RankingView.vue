<template>
  <div class="ranking-view">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <el-button 
          type="text" 
          class="back-btn"
          @click="$router.push('/points')"
        >
          <el-icon><ArrowLeft /></el-icon>
          返回积分中心
        </el-button>
        <h1 class="page-title">
          <el-icon><Trophy /></el-icon>
          积分排行榜
        </h1>
      </div>

      <!-- 当前用户排名卡片 -->
      <el-card v-if="currentUserRank" class="my-rank-card">
        <div class="my-rank-content">
          <div class="my-rank-info">
            <span class="my-rank-label">我的排名</span>
            <span class="my-rank-value">第 {{ currentUserRank }} 名</span>
          </div>
          <div class="my-points-info">
            <span class="my-points-label">我的积分</span>
            <span class="my-points-value">{{ userStore.userInfo?.points || 0 }}</span>
          </div>
        </div>
      </el-card>

      <!-- 排行榜列表 -->
      <el-card class="ranking-card">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>
        
        <div v-else-if="rankings.length === 0" class="empty-container">
          <el-empty description="暂无排行数据" />
        </div>

        <div v-else class="ranking-list">
          <!-- 前三名特殊展示 -->
          <div v-if="topThree.length > 0" class="top-three">
            <div 
              v-for="(item, index) in topThree" 
              :key="item.userId"
              class="top-item"
              :class="[`rank-${index + 1}`, { 'is-current-user': isCurrentUser(item.userId) }]"
            >
              <div class="rank-badge">
                <el-icon v-if="index === 0"><GoldMedal /></el-icon>
                <el-icon v-else-if="index === 1"><Medal /></el-icon>
                <el-icon v-else><Medal /></el-icon>
                <span class="rank-number">{{ index + 1 }}</span>
              </div>
              <el-avatar :size="60" :src="item.userAvatar" class="user-avatar">
                {{ item.userName?.charAt(0) || '?' }}
              </el-avatar>
              <div class="user-name">{{ item.userName }}</div>
              <div class="user-points">{{ item.points }} 积分</div>
            </div>
          </div>
          
          <!-- 其他排名列表 -->
          <div class="other-rankings">
            <div 
              v-for="item in otherRankings" 
              :key="item.userId"
              class="ranking-item"
              :class="{ 'is-current-user': isCurrentUser(item.userId) }"
            >
              <div class="rank-number">{{ item.rank }}</div>
              <el-avatar :size="40" :src="item.userAvatar" class="user-avatar">
                {{ item.userName?.charAt(0) || '?' }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ item.userName }}</div>
              </div>
              <div class="user-points">{{ item.points }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores'
import * as pointApi from '@/api/point'
import type { PointRankVO } from '@/types'
import { ElMessage } from 'element-plus'
import { 
  Trophy, 
  ArrowLeft,
  Medal
} from '@element-plus/icons-vue'

// 自定义金牌图标组件
const GoldMedal = Medal

const userStore = useUserStore()

// 状态
const loading = ref(false)
const rankings = ref<PointRankVO[]>([])

// 计算属性：前三名
const topThree = computed(() => rankings.value.slice(0, 3))

// 计算属性：其他排名（第4名及以后）
const otherRankings = computed(() => rankings.value.slice(3))

// 计算属性：当前用户排名
const currentUserRank = computed(() => {
  const userId = userStore.userInfo?.id
  if (!userId) return null
  const userRanking = rankings.value.find(r => r.userId === userId)
  return userRanking?.rank || null
})

// 判断是否为当前用户
const isCurrentUser = (userId: number) => {
  return userStore.userInfo?.id === userId
}

// 获取排行榜数据
const fetchRankings = async () => {
  loading.value = true
  try {
    const response = await pointApi.getPointRanking({ limit: 100 })
    if (response.code === 200 && response.data) {
      rankings.value = response.data
    } else {
      ElMessage.error(response.message || '获取排行榜失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取排行榜失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRankings()
})
</script>

<style scoped lang="scss">
.ranking-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  
  .container {
    max-width: 800px;
    margin: 0 auto;
  }
}

.page-header {
  margin-bottom: 20px;
  
  .back-btn {
    margin-bottom: 12px;
    padding: 0;
    color: #606266;
    
    &:hover {
      color: #409eff;
    }
    
    .el-icon {
      margin-right: 4px;
    }
  }
  
  .page-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    
    .el-icon {
      color: #e6a23c;
    }
  }
}

.my-rank-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .my-rank-content {
    display: flex;
    justify-content: space-around;
    color: #fff;
    
    .my-rank-info,
    .my-points-info {
      text-align: center;
      
      .my-rank-label,
      .my-points-label {
        font-size: 14px;
        opacity: 0.9;
        display: block;
        margin-bottom: 8px;
      }
      
      .my-rank-value,
      .my-points-value {
        font-size: 28px;
        font-weight: 700;
      }
    }
  }
}

.ranking-card {
  .loading-container,
  .empty-container {
    padding: 40px 0;
  }
}

.ranking-list {
  .top-three {
    display: flex;
    justify-content: center;
    gap: 20px;
    padding: 30px 0;
    border-bottom: 1px solid #ebeef5;
    
    .top-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      border-radius: 12px;
      transition: transform 0.3s, box-shadow 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      &.is-current-user {
        background: linear-gradient(135deg, #e8f4fd 0%, #d4e8f9 100%);
        border: 2px solid #409eff;
      }
      
      .rank-badge {
        position: relative;
        margin-bottom: 12px;
        
        .el-icon {
          font-size: 36px;
        }
        
        .rank-number {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          font-size: 14px;
          font-weight: 700;
          color: #fff;
        }
      }
      
      &.rank-1 {
        order: 2;
        
        .rank-badge .el-icon {
          color: #ffd700;
          font-size: 48px;
        }
        
        .user-avatar {
          width: 80px;
          height: 80px;
          border: 3px solid #ffd700;
        }
      }
      
      &.rank-2 {
        order: 1;
        
        .rank-badge .el-icon {
          color: #c0c0c0;
        }
        
        .user-avatar {
          border: 3px solid #c0c0c0;
        }
      }
      
      &.rank-3 {
        order: 3;
        
        .rank-badge .el-icon {
          color: #cd7f32;
        }
        
        .user-avatar {
          border: 3px solid #cd7f32;
        }
      }
      
      .user-avatar {
        margin-bottom: 10px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        font-size: 24px;
      }
      
      .user-name {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .user-points {
        font-size: 16px;
        font-weight: 700;
        color: #e6a23c;
      }
    }
  }

  .other-rankings {
    padding: 10px 0;
    
    .ranking-item {
      display: flex;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid #ebeef5;
      transition: background-color 0.3s;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      &.is-current-user {
        background: linear-gradient(135deg, #e8f4fd 0%, #d4e8f9 100%);
        border-left: 4px solid #409eff;
      }
      
      .rank-number {
        width: 40px;
        font-size: 16px;
        font-weight: 600;
        color: #909399;
        text-align: center;
      }
      
      .user-avatar {
        margin: 0 16px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      
      .user-info {
        flex: 1;
        
        .user-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
        }
      }
      
      .user-points {
        font-size: 16px;
        font-weight: 700;
        color: #e6a23c;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .ranking-view {
    padding: 10px;
  }
  
  .page-header {
    .page-title {
      font-size: 20px;
    }
  }
  
  .my-rank-card {
    .my-rank-content {
      .my-rank-info,
      .my-points-info {
        .my-rank-value,
        .my-points-value {
          font-size: 22px;
        }
      }
    }
  }
  
  .ranking-list {
    .top-three {
      flex-wrap: wrap;
      gap: 10px;
      
      .top-item {
        padding: 15px;
        
        &.rank-1 {
          .user-avatar {
            width: 60px;
            height: 60px;
          }
        }
      }
    }
    
    .other-rankings {
      .ranking-item {
        padding: 12px 10px;
        
        .rank-number {
          width: 30px;
          font-size: 14px;
        }
        
        .user-avatar {
          margin: 0 10px;
        }
        
        .user-points {
          font-size: 14px;
        }
      }
    }
  }
}
</style>
