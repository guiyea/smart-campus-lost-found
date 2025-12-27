<template>
  <div class="point-view">
    <div class="container">
      <!-- 顶部积分概览 -->
      <el-card class="points-overview-card">
        <div class="points-overview">
          <div class="points-main">
            <div class="points-icon">
              <el-icon :size="48"><Medal /></el-icon>
            </div>
            <div class="points-info">
              <div class="points-value">{{ totalPoints }}</div>
              <div class="points-label">当前总积分</div>
            </div>
          </div>
          <div class="points-actions">
            <el-button type="primary" @click="$router.push('/points/ranking')">
              <el-icon><Trophy /></el-icon>
              查看排行榜
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 积分规则说明 -->
      <el-card class="rules-card">
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>积分规则说明</span>
          </div>
        </template>
        <div class="rules-list">
          <div class="rule-item">
            <div class="rule-icon daily-login">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="rule-content">
              <div class="rule-title">每日登录</div>
              <div class="rule-desc">每天首次登录系统可获得积分</div>
            </div>
            <div class="rule-points">+2</div>
          </div>
          <div class="rule-item">
            <div class="rule-icon publish-found">
              <el-icon><Upload /></el-icon>
            </div>
            <div class="rule-content">
              <div class="rule-title">发布招领</div>
              <div class="rule-desc">发布有效的招领信息可获得积分</div>
            </div>
            <div class="rule-points">+10</div>
          </div>
          <div class="rule-item">
            <div class="rule-icon help-find">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="rule-content">
              <div class="rule-title">帮助找回</div>
              <div class="rule-desc">成功帮助他人找回物品可获得积分</div>
            </div>
            <div class="rule-points">+50</div>
          </div>
        </div>
      </el-card>

      <!-- 积分明细列表 -->
      <el-card class="records-card">
        <template #header>
          <div class="card-header">
            <el-icon><List /></el-icon>
            <span>积分明细</span>
          </div>
        </template>
        
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="records.length === 0" class="empty-container">
          <el-empty description="暂无积分记录" />
        </div>
        
        <div v-else class="records-list">
          <div 
            v-for="record in records" 
            :key="record.id" 
            class="record-item"
          >
            <div class="record-left">
              <div 
                class="record-icon" 
                :class="getRecordIconClass(record.reason)"
              >
                <el-icon><component :is="getRecordIcon(record.reason)" /></el-icon>
              </div>
              <div class="record-info">
                <div class="record-reason">{{ record.reasonDesc }}</div>
                <div class="record-time">{{ formatTime(record.createdAt) }}</div>
              </div>
            </div>
            <div 
              class="record-points" 
              :class="record.points > 0 ? 'positive' : 'negative'"
            >
              {{ record.points > 0 ? '+' : '' }}{{ record.points }}
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores'
import * as pointApi from '@/api/point'
import type { PointRecordVO } from '@/types'
import { ElMessage } from 'element-plus'
import { 
  Medal, 
  Trophy, 
  InfoFilled, 
  Calendar, 
  Upload, 
  CircleCheck,
  List
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 状态
const loading = ref(false)
const records = ref<PointRecordVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPoints = computed(() => userStore.userInfo?.points || 0)

// 获取积分记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const response = await pointApi.getPointRecords({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (response.code === 200 && response.data) {
      records.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取积分记录失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取积分记录失败')
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchRecords()
}

// 处理页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchRecords()
}

// 根据原因获取图标
const getRecordIcon = (reason: string) => {
  switch (reason) {
    case 'DAILY_LOGIN':
      return Calendar
    case 'PUBLISH_FOUND':
      return Upload
    case 'HELP_FIND':
      return CircleCheck
    default:
      return Medal
  }
}

// 根据原因获取图标样式类
const getRecordIconClass = (reason: string) => {
  switch (reason) {
    case 'DAILY_LOGIN':
      return 'daily-login'
    case 'PUBLISH_FOUND':
      return 'publish-found'
    case 'HELP_FIND':
      return 'help-find'
    default:
      return 'default'
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  fetchRecords()
})
</script>


<style scoped lang="scss">
.point-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  
  .container {
    max-width: 800px;
    margin: 0 auto;
  }
}

.points-overview-card {
  margin-bottom: 20px;
  
  .points-overview {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .points-main {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .points-icon {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        
        .el-icon {
          color: #e6a23c;
        }
      }
      
      .points-info {
        .points-value {
          font-size: 42px;
          font-weight: 700;
          color: #e6a23c;
          line-height: 1.2;
        }
        
        .points-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
}

.rules-card {
  margin-bottom: 20px;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    
    .el-icon {
      color: #409eff;
    }
  }
  
  .rules-list {
    .rule-item {
      display: flex;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .rule-icon {
        width: 44px;
        height: 44px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        
        .el-icon {
          font-size: 22px;
          color: #fff;
        }
        
        &.daily-login {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        
        &.publish-found {
          background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
        }
        
        &.help-find {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }
      }
      
      .rule-content {
        flex: 1;
        
        .rule-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .rule-desc {
          font-size: 13px;
          color: #909399;
        }
      }
      
      .rule-points {
        font-size: 20px;
        font-weight: 700;
        color: #67c23a;
      }
    }
  }
}

.records-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    
    .el-icon {
      color: #409eff;
    }
  }
  
  .loading-container,
  .empty-container {
    padding: 40px 0;
  }
  
  .records-list {
    .record-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .record-left {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .record-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          .el-icon {
            font-size: 18px;
            color: #fff;
          }
          
          &.daily-login {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }
          
          &.publish-found {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
          }
          
          &.help-find {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }
          
          &.default {
            background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
            
            .el-icon {
              color: #e6a23c;
            }
          }
        }
        
        .record-info {
          .record-reason {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }
          
          .record-time {
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      .record-points {
        font-size: 18px;
        font-weight: 700;
        
        &.positive {
          color: #67c23a;
        }
        
        &.negative {
          color: #f56c6c;
        }
      }
    }
  }
  
  .pagination-container {
    padding-top: 20px;
    display: flex;
    justify-content: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .point-view {
    padding: 10px;
  }
  
  .points-overview-card {
    .points-overview {
      flex-direction: column;
      gap: 20px;
      
      .points-main {
        .points-icon {
          width: 60px;
          height: 60px;
          
          .el-icon {
            font-size: 32px;
          }
        }
        
        .points-info {
          .points-value {
            font-size: 32px;
          }
        }
      }
      
      .points-actions {
        width: 100%;
        
        .el-button {
          width: 100%;
        }
      }
    }
  }
  
  .rules-card {
    .rules-list {
      .rule-item {
        .rule-icon {
          width: 36px;
          height: 36px;
          
          .el-icon {
            font-size: 18px;
          }
        }
        
        .rule-points {
          font-size: 16px;
        }
      }
    }
  }
  
  .records-card {
    .records-list {
      .record-item {
        .record-left {
          .record-icon {
            width: 36px;
            height: 36px;
            
            .el-icon {
              font-size: 16px;
            }
          }
        }
        
        .record-points {
          font-size: 16px;
        }
      }
    }
  }
}
</style>
