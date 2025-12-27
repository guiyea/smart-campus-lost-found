<template>
  <div class="admin-users">
    <!-- 搜索筛选区域 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :model="searchForm" inline class="filter-form">
        <el-form-item label="学号">
          <el-input
            v-model="searchForm.studentId"
            placeholder="搜索学号"
            clearable
            style="width: 160px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.name"
            placeholder="搜索姓名"
            clearable
            style="width: 160px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="已封禁" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <span class="total-count">共 {{ total }} 条记录</span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentId" label="学号" width="140" align="center" />
        <el-table-column prop="name" label="姓名" width="120" align="center">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.avatar" :size="28">
                {{ row.name?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column prop="points" label="积分" width="100" align="center">
          <template #default="{ row }">
            <span class="points-value">{{ row.points || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'warning' : 'primary'" size="small">
              {{ row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'" size="small">
              {{ row.status === 1 ? '已封禁' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status !== 1"
              type="danger"
              link
              size="small"
              @click="handleBan(row)"
            >
              封禁
            </el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              @click="handleUnban(row)"
            >
              解封
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

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="500px"
      destroy-on-close
    >
      <div v-if="currentUser" class="user-detail">
        <div class="user-avatar-section">
          <el-avatar :src="currentUser.avatar" :size="80">
            {{ currentUser.name?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-basic-info">
            <h3>{{ currentUser.name }}</h3>
            <p class="student-id">学号：{{ currentUser.studentId }}</p>
          </div>
        </div>
        
        <el-descriptions :column="1" border class="user-descriptions">
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="积分">
            <span class="points-value">{{ currentUser.points || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="currentUser.role === 1 ? 'warning' : 'primary'" size="small">
              {{ currentUser.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'danger' : 'success'" size="small">
              {{ currentUser.status === 1 ? '已封禁' : '正常' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDateTime(currentUser.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentUser && currentUser.status !== 1"
          type="danger"
          @click="handleBanFromDetail"
        >
          封禁用户
        </el-button>
        <el-button
          v-else-if="currentUser"
          type="success"
          @click="handleUnbanFromDetail"
        >
          解封用户
        </el-button>
      </template>
    </el-dialog>

    <!-- 封禁原因对话框 -->
    <el-dialog
      v-model="banDialogVisible"
      title="封禁用户"
      width="450px"
      destroy-on-close
    >
      <el-form :model="banForm" label-width="80px">
        <el-form-item label="用户">
          <span>{{ banTargetUser?.name }} ({{ banTargetUser?.studentId }})</span>
        </el-form-item>
        <el-form-item label="封禁原因" required>
          <el-input
            v-model="banForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入封禁原因（必填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="banLoading" @click="confirmBan">
          确认封禁
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserList, banUser, unbanUser, type UserSearchParams } from '../../api/admin'
import type { UserVO } from '../../types'

// 搜索表单
const searchForm = reactive<UserSearchParams>({
  studentId: '',
  name: '',
  status: undefined,
  pageNum: 1,
  pageSize: 20
})

// 列表数据
const userList = ref<UserVO[]>([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentUser = ref<UserVO | null>(null)

// 封禁对话框
const banDialogVisible = ref(false)
const banTargetUser = ref<UserVO | null>(null)
const banForm = reactive({
  reason: ''
})
const banLoading = ref(false)

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const res = await getAdminUserList(searchForm)
    if (res.code === 200 && res.data) {
      userList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  loadUserList()
}

// 重置
const handleReset = () => {
  searchForm.studentId = ''
  searchForm.name = ''
  searchForm.status = undefined
  searchForm.pageNum = 1
  loadUserList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  searchForm.pageSize = size
  searchForm.pageNum = 1
  loadUserList()
}

// 页码改变
const handleCurrentChange = (page: number) => {
  searchForm.pageNum = page
  loadUserList()
}

// 查看详情
const handleViewDetail = (row: UserVO) => {
  currentUser.value = row
  detailDialogVisible.value = true
}

// 封禁用户（从列表）
const handleBan = (row: UserVO) => {
  banTargetUser.value = row
  banForm.reason = ''
  banDialogVisible.value = true
}

// 封禁用户（从详情对话框）
const handleBanFromDetail = () => {
  if (currentUser.value) {
    banTargetUser.value = currentUser.value
    banForm.reason = ''
    detailDialogVisible.value = false
    banDialogVisible.value = true
  }
}

// 确认封禁
const confirmBan = async () => {
  if (!banForm.reason.trim()) {
    ElMessage.warning('请输入封禁原因')
    return
  }
  
  if (!banTargetUser.value) return
  
  banLoading.value = true
  try {
    const res = await banUser(banTargetUser.value.id, banForm.reason)
    if (res.code === 200) {
      ElMessage.success('封禁成功')
      banDialogVisible.value = false
      loadUserList()
    } else {
      ElMessage.error(res.message || '封禁失败')
    }
  } catch (error) {
    console.error('封禁用户失败:', error)
    ElMessage.error('封禁失败')
  } finally {
    banLoading.value = false
  }
}

// 解封用户（从列表）
const handleUnban = async (row: UserVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要解封用户「${row.name}」吗？`,
      '确认解封',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const res = await unbanUser(row.id)
    if (res.code === 200) {
      ElMessage.success('解封成功')
      loadUserList()
    } else {
      ElMessage.error(res.message || '解封失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解封用户失败:', error)
      ElMessage.error('解封失败')
    }
  }
}

// 解封用户（从详情对话框）
const handleUnbanFromDetail = async () => {
  if (!currentUser.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要解封用户「${currentUser.value.name}」吗？`,
      '确认解封',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const res = await unbanUser(currentUser.value.id)
    if (res.code === 200) {
      ElMessage.success('解封成功')
      detailDialogVisible.value = false
      loadUserList()
    } else {
      ElMessage.error(res.message || '解封失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解封用户失败:', error)
      ElMessage.error('解封失败')
    }
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
  loadUserList()
})
</script>

<style scoped lang="scss">
.admin-users {
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

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .user-name {
    font-weight: 500;
  }
}

.points-value {
  color: #e6a23c;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.user-detail {
  .user-avatar-section {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;
    
    .user-basic-info {
      h3 {
        margin: 0 0 8px 0;
        font-size: 18px;
        color: #303133;
      }
      
      .student-id {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
  
  .user-descriptions {
    :deep(.el-descriptions__label) {
      width: 100px;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .admin-users {
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
  
  .user-detail {
    .user-avatar-section {
      flex-direction: column;
      text-align: center;
    }
  }
}
</style>
