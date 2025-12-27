<template>
  <div class="admin-reports">
    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :model="searchForm" inline class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索标题/描述"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="失物" :value="0" />
            <el-option label="招领" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="已找回" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 待审核物品列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>待审核物品列表</span>
          <span class="total-count">共 {{ total }} 条记录</span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="itemList"
        stripe
        border
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.images && row.images.length > 0"
              :src="row.images[0]"
              :preview-src-list="row.images"
              fit="cover"
              class="item-thumbnail"
            />
            <span v-else class="no-image">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'danger' : 'success'" size="small">
              {{ row.type === 0 ? '失物' : '招领' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="userName" label="发布者" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发布时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="审核操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleReview(row, 0)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="handleReview(row, 1)">
              删除
            </el-button>
            <el-button type="warning" size="small" @click="handleReview(row, 2)">
              警告
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 物品详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="物品详情"
      width="700px"
      destroy-on-close
    >
      <div v-if="currentItem" class="item-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="currentItem.type === 0 ? 'danger' : 'success'" size="small">
              {{ currentItem.type === 0 ? '失物' : '招领' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="类别">{{ currentItem.category || '未分类' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentItem.status)" size="small">
              {{ getStatusText(currentItem.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.userName }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ currentItem.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="地点" :span="2">{{ currentItem.locationDesc }}</el-descriptions-item>
          <el-descriptions-item label="事件时间">{{ formatDateTime(currentItem.eventTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDateTime(currentItem.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentItem.description }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 图片展示 -->
        <div v-if="currentItem.images && currentItem.images.length > 0" class="images-section">
          <h4>物品图片</h4>
          <div class="image-list">
            <el-image
              v-for="(img, index) in currentItem.images"
              :key="index"
              :src="img"
              :preview-src-list="currentItem.images"
              :initial-index="index"
              fit="cover"
              class="item-image"
            />
          </div>
        </div>
        
        <!-- 标签展示 -->
        <div v-if="currentItem.tags && currentItem.tags.length > 0" class="tags-section">
          <h4>物品标签</h4>
          <el-tag v-for="tag in currentItem.tags" :key="tag" class="tag-item" size="small">
            {{ tag }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="handleReviewFromDetail(0)">通过</el-button>
        <el-button type="danger" @click="handleReviewFromDetail(1)">删除</el-button>
        <el-button type="warning" @click="handleReviewFromDetail(2)">警告</el-button>
      </template>
    </el-dialog>

    <!-- 审核原因对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="getReviewDialogTitle()"
      width="500px"
      destroy-on-close
    >
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="物品标题">
          <span>{{ reviewForm.itemTitle }}</span>
        </el-form-item>
        <el-form-item label="审核操作">
          <el-tag :type="getActionTagType(reviewForm.action)" size="default">
            {{ getActionText(reviewForm.action) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核原因" :required="reviewForm.action !== 0">
          <el-input
            v-model="reviewForm.reason"
            type="textarea"
            :rows="4"
            :placeholder="getReasonPlaceholder()"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="submitReview">
          确认{{ getActionText(reviewForm.action) }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdminItemList, reviewItem } from '../../api/admin'
import type { ItemVO, ItemAdminSearchDTO } from '../../types'

// 搜索表单
const searchForm = reactive<ItemAdminSearchDTO>({
  keyword: '',
  type: undefined,
  status: undefined,
  deleted: 0, // 只查询未删除的物品
  pageNum: 1,
  pageSize: 20
})

// 列表数据
const itemList = ref<ItemVO[]>([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentItem = ref<ItemVO | null>(null)

// 审核对话框
const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewForm = reactive({
  itemId: 0,
  itemTitle: '',
  action: 0, // 0-通过, 1-删除, 2-警告
  reason: ''
})

// 加载物品列表
const loadItemList = async () => {
  loading.value = true
  try {
    const res = await getAdminItemList(searchForm)
    if (res.code === 200 && res.data) {
      itemList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取物品列表失败')
    }
  } catch (error) {
    console.error('获取物品列表失败:', error)
    ElMessage.error('获取物品列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  loadItemList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.type = undefined
  searchForm.status = undefined
  searchForm.pageNum = 1
  loadItemList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  searchForm.pageSize = size
  searchForm.pageNum = 1
  loadItemList()
}

// 页码改变
const handleCurrentChange = (page: number) => {
  searchForm.pageNum = page
  loadItemList()
}

// 查看详情
const handleViewDetail = (row: ItemVO) => {
  currentItem.value = row
  detailDialogVisible.value = true
}

// 审核操作
const handleReview = (row: ItemVO, action: number) => {
  reviewForm.itemId = row.id
  reviewForm.itemTitle = row.title
  reviewForm.action = action
  reviewForm.reason = ''
  reviewDialogVisible.value = true
}

// 从详情页审核
const handleReviewFromDetail = (action: number) => {
  if (currentItem.value) {
    detailDialogVisible.value = false
    handleReview(currentItem.value, action)
  }
}

// 提交审核
const submitReview = async () => {
  // 删除和警告操作需要填写原因
  if (reviewForm.action !== 0 && !reviewForm.reason.trim()) {
    ElMessage.warning('请填写审核原因')
    return
  }
  
  reviewLoading.value = true
  try {
    const res = await reviewItem(reviewForm.itemId, reviewForm.action, reviewForm.reason || undefined)
    if (res.code === 200) {
      ElMessage.success(`${getActionText(reviewForm.action)}成功`)
      reviewDialogVisible.value = false
      loadItemList()
    } else {
      ElMessage.error(res.message || '审核操作失败')
    }
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage.error('审核操作失败')
  } finally {
    reviewLoading.value = false
  }
}

// 获取审核对话框标题
const getReviewDialogTitle = () => {
  switch (reviewForm.action) {
    case 0: return '审核通过'
    case 1: return '审核删除'
    case 2: return '发送警告'
    default: return '审核操作'
  }
}

// 获取操作文本
const getActionText = (action: number) => {
  switch (action) {
    case 0: return '通过'
    case 1: return '删除'
    case 2: return '警告'
    default: return '未知'
  }
}

// 获取操作标签类型
const getActionTagType = (action: number): 'success' | 'danger' | 'warning' | 'primary' => {
  switch (action) {
    case 0: return 'success'
    case 1: return 'danger'
    case 2: return 'warning'
    default: return 'primary'
  }
}

// 获取原因占位符
const getReasonPlaceholder = () => {
  switch (reviewForm.action) {
    case 0: return '可选填写审核备注'
    case 1: return '请填写删除原因，如：内容违规、虚假信息等'
    case 2: return '请填写警告原因，将发送给发布者'
    default: return '请填写原因'
  }
}

// 获取状态类型
const getStatusType = (status: number): 'warning' | 'success' | 'info' | 'primary' => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'primary'
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

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadItemList()
})
</script>

<style scoped lang="scss">
.admin-reports {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 10px;
    }
  }
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span:first-child {
      font-size: 16px;
      font-weight: 500;
    }
    
    .total-count {
      font-size: 14px;
      color: #909399;
    }
  }
}

.item-thumbnail {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.no-image {
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.item-detail {
  .images-section {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 10px;
      color: #303133;
    }
    
    .image-list {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      
      .item-image {
        width: 100px;
        height: 100px;
        border-radius: 4px;
        cursor: pointer;
      }
    }
  }
  
  .tags-section {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 10px;
      color: #303133;
    }
    
    .tag-item {
      margin-right: 8px;
      margin-bottom: 8px;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .admin-reports {
    padding: 10px;
  }
  
  .filter-card {
    .filter-form {
      :deep(.el-form-item) {
        width: 100%;
        margin-right: 0;
      }
      
      :deep(.el-input),
      :deep(.el-select) {
        width: 100% !important;
      }
    }
  }
}
</style>
