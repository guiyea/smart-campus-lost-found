<template>
  <div class="admin-items">
    <!-- 搜索筛选区域 -->
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
        <el-form-item label="删除状态">
          <el-select v-model="searchForm.deleted" placeholder="全部" clearable style="width: 120px">
            <el-option label="未删除" :value="0" />
            <el-option label="已删除" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 物品列表表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>物品列表</span>
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
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="删除状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.deleted === 1 ? 'info' : 'success'" size="small">
              {{ row.deleted === 1 ? '已删除' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="发布者" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="viewCount" label="浏览量" width="80" align="center" />
        <el-table-column prop="createdAt" label="发布时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button
              v-if="row.deleted !== 1"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              @click="handleRestore(row)"
            >
              恢复
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
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminItemList, reviewItem, restoreItem } from '../../api/admin'
import type { ItemVO, ItemAdminSearchDTO } from '../../types'

// 搜索表单
const searchForm = reactive<ItemAdminSearchDTO>({
  keyword: '',
  type: undefined,
  status: undefined,
  deleted: undefined,
  startTime: undefined,
  endTime: undefined,
  pageNum: 1,
  pageSize: 20
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 列表数据
const itemList = ref<ItemVO[]>([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentItem = ref<ItemVO | null>(null)

// 加载物品列表
const loadItemList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.startTime = dateRange.value[0]
      searchForm.endTime = dateRange.value[1]
    } else {
      searchForm.startTime = undefined
      searchForm.endTime = undefined
    }
    
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
  searchForm.deleted = undefined
  searchForm.startTime = undefined
  searchForm.endTime = undefined
  searchForm.pageNum = 1
  dateRange.value = null
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

// 删除物品
const handleDelete = async (row: ItemVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除物品「${row.title}」吗？删除后可以恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await reviewItem(row.id, 1, '管理员删除')
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadItemList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除物品失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 恢复物品
const handleRestore = async (row: ItemVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要恢复物品「${row.title}」吗？`,
      '确认恢复',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const res = await restoreItem(row.id)
    if (res.code === 200) {
      ElMessage.success('恢复成功')
      loadItemList()
    } else {
      ElMessage.error(res.message || '恢复失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复物品失败:', error)
      ElMessage.error('恢复失败')
    }
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
.admin-items {
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
  .admin-items {
    padding: 10px;
  }
  
  .filter-card {
    .filter-form {
      :deep(.el-form-item) {
        width: 100%;
        margin-right: 0;
      }
      
      :deep(.el-input),
      :deep(.el-select),
      :deep(.el-date-editor) {
        width: 100% !important;
      }
    }
  }
}
</style>
