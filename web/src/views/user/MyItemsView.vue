<template>
  <div class="my-items-page">
    <div class="container">
      <el-card class="items-card">
        <template #header>
          <div class="card-header">
            <span>我的发布</span>
            <el-button type="primary" @click="$router.push('/publish')">
              <el-icon><Plus /></el-icon>
              发布新信息
            </el-button>
          </div>
        </template>
        
        <!-- Tab切换：我的失物、我的招领 -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="我的失物" name="lost">
            <template #label>
              <span class="tab-label">
                <el-icon><Search /></el-icon>
                我的失物
                <el-badge :value="lostCount" :hidden="lostCount === 0" class="tab-badge" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="我的招领" name="found">
            <template #label>
              <span class="tab-label">
                <el-icon><Box /></el-icon>
                我的招领
                <el-badge :value="foundCount" :hidden="foundCount === 0" class="tab-badge" />
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
        
        <!-- 状态筛选 -->
        <div class="filter-section">
          <el-radio-group v-model="statusFilter" @change="handleFilterChange">
            <el-radio-button :label="undefined">全部</el-radio-button>
            <el-radio-button :label="0">待处理</el-radio-button>
            <el-radio-button :label="1">已找回</el-radio-button>
            <el-radio-button :label="2">已关闭</el-radio-button>
          </el-radio-group>
        </div>
        
        <!-- 物品列表 -->
        <div v-loading="loading" class="items-list">
          <template v-if="items.length > 0">
            <el-row :gutter="16">
              <el-col 
                v-for="item in items" 
                :key="item.id" 
                :xs="24" 
                :sm="12" 
                :md="8" 
                :lg="6"
              >
                <el-card class="item-card" shadow="hover" @click="viewDetail(item.id)">
                  <!-- 物品图片 -->
                  <div class="item-image">
                    <el-image
                      :src="item.images?.[0] || defaultImage"
                      fit="cover"
                      class="image"
                    >
                      <template #error>
                        <div class="image-placeholder">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                    <!-- 状态标签 -->
                    <el-tag 
                      :type="getStatusType(item.status)" 
                      class="status-tag"
                      size="small"
                    >
                      {{ getStatusText(item.status) }}
                    </el-tag>
                    <!-- 类型标签 -->
                    <el-tag 
                      :type="item.type === 0 ? 'danger' : 'success'" 
                      class="type-tag"
                      size="small"
                    >
                      {{ item.type === 0 ? '失物' : '招领' }}
                    </el-tag>
                  </div>
                  
                  <!-- 物品信息 -->
                  <div class="item-info">
                    <h3 class="item-title">{{ item.title }}</h3>
                    <p class="item-location">
                      <el-icon><Location /></el-icon>
                      {{ item.locationDesc }}
                    </p>
                    <p class="item-time">
                      <el-icon><Clock /></el-icon>
                      {{ formatDate(item.eventTime) }}
                    </p>
                    <div class="item-stats">
                      <span class="stat-item">
                        <el-icon><View /></el-icon>
                        {{ item.viewCount }}
                      </span>
                    </div>
                  </div>
                  
                  <!-- 操作按钮 -->
                  <div class="item-actions" @click.stop>
                    <el-button 
                      type="primary" 
                      size="small" 
                      text
                      @click.stop="viewDetail(item.id)"
                    >
                      查看
                    </el-button>
                    <el-button 
                      v-if="item.status === 0"
                      type="warning" 
                      size="small" 
                      text
                      @click.stop="editItem(item.id)"
                    >
                      编辑
                    </el-button>
                    <el-popconfirm
                      title="确定要删除这条信息吗？"
                      confirm-button-text="确定"
                      cancel-button-text="取消"
                      @confirm="deleteItem(item.id)"
                    >
                      <template #reference>
                        <el-button 
                          type="danger" 
                          size="small" 
                          text
                          @click.stop
                        >
                          删除
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </template>
          
          <!-- 空状态 -->
          <el-empty v-else :description="emptyText">
            <el-button type="primary" @click="$router.push('/publish')">
              立即发布
            </el-button>
          </el-empty>
        </div>
        
        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-section">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[8, 12, 20, 40]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import * as itemApi from '@/api/item'
import type { ItemVO } from '@/types'
import { ElMessage } from 'element-plus'
import { 
  Plus, 
  Search, 
  Box, 
  Location, 
  Clock, 
  View, 
  Picture 
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 默认图片
const defaultImage = 'https://via.placeholder.com/300x200?text=No+Image'

// 当前Tab
const activeTab = ref<'lost' | 'found'>('lost')

// 状态筛选
const statusFilter = ref<number | undefined>(undefined)

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 加载状态
const loading = ref(false)

// 物品列表
const items = ref<ItemVO[]>([])

// 统计数量
const lostCount = ref(0)
const foundCount = ref(0)

// 空状态文本
const emptyText = computed(() => {
  if (activeTab.value === 'lost') {
    return '暂无失物信息'
  }
  return '暂无招领信息'
})

// 获取状态类型
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '待处理'
    case 1: return '已找回'
    case 2: return '已关闭'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 获取物品列表
const fetchItems = async () => {
  loading.value = true
  try {
    const type = activeTab.value === 'lost' ? 0 : 1
    const response = await itemApi.searchItems({
      type,
      status: statusFilter.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (response.code === 200 && response.data) {
      // 过滤出当前用户的物品
      const userId = userStore.userInfo?.id
      items.value = response.data.records.filter(item => item.userId === userId)
      total.value = items.value.length
      
      // 注意：实际应该后端提供按用户筛选的接口
      // 这里暂时前端过滤，实际项目中应该调用专门的"我的发布"接口
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数量
const fetchCounts = async () => {
  try {
    const userId = userStore.userInfo?.id
    
    // 获取失物数量
    const lostResponse = await itemApi.searchItems({ type: 0, pageSize: 1000 })
    if (lostResponse.code === 200 && lostResponse.data) {
      lostCount.value = lostResponse.data.records.filter(item => item.userId === userId).length
    }
    
    // 获取招领数量
    const foundResponse = await itemApi.searchItems({ type: 1, pageSize: 1000 })
    if (foundResponse.code === 200 && foundResponse.data) {
      foundCount.value = foundResponse.data.records.filter(item => item.userId === userId).length
    }
  } catch (error) {
    console.error('获取统计数量失败:', error)
  }
}

// Tab切换
const handleTabChange = () => {
  currentPage.value = 1
  fetchItems()
}

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchItems()
}

// 分页大小变化
const handleSizeChange = () => {
  currentPage.value = 1
  fetchItems()
}

// 页码变化
const handlePageChange = () => {
  fetchItems()
}

// 查看详情
const viewDetail = (id: number) => {
  router.push(`/item/${id}`)
}

// 编辑物品
const editItem = (id: number) => {
  router.push(`/item/${id}/edit`)
}

// 删除物品
const deleteItem = async (id: number) => {
  try {
    const response = await itemApi.deleteItem(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchItems()
      fetchCounts()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

onMounted(() => {
  fetchItems()
  fetchCounts()
})

// 监听用户登录状态
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    fetchItems()
    fetchCounts()
  }
})
</script>

<style scoped lang="scss">
.my-items-page {
  padding: 20px;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
}

.items-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .tab-badge {
    margin-left: 4px;
  }
}

.filter-section {
  margin: 16px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.items-list {
  min-height: 300px;
}

.item-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  
  &:hover {
    transform: translateY(-4px);
  }
  
  :deep(.el-card__body) {
    padding: 0;
  }
  
  .item-image {
    position: relative;
    height: 160px;
    overflow: hidden;
    
    .image {
      width: 100%;
      height: 100%;
    }
    
    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #c0c4cc;
      font-size: 40px;
    }
    
    .status-tag {
      position: absolute;
      top: 8px;
      right: 8px;
    }
    
    .type-tag {
      position: absolute;
      top: 8px;
      left: 8px;
    }
  }
  
  .item-info {
    padding: 12px;
    
    .item-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .item-location,
    .item-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #909399;
      margin: 4px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      
      .el-icon {
        flex-shrink: 0;
      }
    }
    
    .item-stats {
      display: flex;
      gap: 12px;
      margin-top: 8px;
      
      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .item-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 8px 12px;
    border-top: 1px solid #ebeef5;
    background: #fafafa;
  }
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

// 响应式设计
@media (max-width: 768px) {
  .my-items-page {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    
    .el-button {
      width: 100%;
    }
  }
  
  .filter-section {
    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
  
  .pagination-section {
    :deep(.el-pagination) {
      flex-wrap: wrap;
      justify-content: center;
    }
  }
}
</style>
