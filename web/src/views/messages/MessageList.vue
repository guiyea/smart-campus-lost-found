<template>
  <div class="messages-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>消息中心</h1>
        <p>查看系统通知、匹配通知和留言消息</p>
      </div>

      <el-card class="messages-card">
        <!-- 顶部操作栏 -->
        <div class="toolbar">
          <!-- Tab切换 -->
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部消息" name="all">
              <template #label>
                <span>全部消息</span>
                <el-badge v-if="totalUnread > 0" :value="totalUnread" class="tab-badge" />
              </template>
            </el-tab-pane>
            <el-tab-pane label="系统通知" name="system">
              <template #label>
                <span>系统通知</span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="匹配通知" name="match">
              <template #label>
                <span>匹配通知</span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="留言通知" name="comment">
              <template #label>
                <span>留言通知</span>
              </template>
            </el-tab-pane>
          </el-tabs>

          <!-- 操作按钮 -->
          <div class="actions">
            <el-button 
              type="primary" 
              plain 
              :disabled="totalUnread === 0"
              @click="handleMarkAllRead"
            >
              <el-icon><Check /></el-icon>
              全部已读
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-loading="loading" class="message-list">
          <template v-if="messages.length > 0">
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="{ unread: !message.isRead }"
              @click="handleMessageClick(message)"
            >
              <!-- 消息图标 -->
              <div class="message-icon">
                <el-icon :size="24" :class="getMessageIconClass(message.type)">
                  <component :is="getMessageIcon(message.type)" />
                </el-icon>
              </div>

              <!-- 消息内容 -->
              <div class="message-content">
                <div class="message-header">
                  <span class="message-title">{{ message.title }}</span>
                  <el-tag 
                    :type="getMessageTagType(message.type)" 
                    size="small"
                    class="message-type"
                  >
                    {{ message.typeName }}
                  </el-tag>
                </div>
                <div class="message-body">
                  {{ truncateContent(message.content) }}
                </div>
                <div class="message-footer">
                  <span class="message-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatTime(message.createdAt) }}
                  </span>
                  <span v-if="!message.isRead" class="unread-dot"></span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="message-actions">
                <el-button
                  v-if="!message.isRead"
                  type="primary"
                  text
                  size="small"
                  @click.stop="handleMarkRead(message)"
                >
                  标记已读
                </el-button>
              </div>
            </div>
          </template>

          <!-- 空状态 -->
          <el-empty v-else description="暂无消息" />
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-section">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Check, 
  Clock, 
  Bell, 
  Connection, 
  ChatDotRound,
  Notification
} from '@element-plus/icons-vue'
import * as messageApi from '@/api/message'
import type { MessageVO } from '@/types'

const router = useRouter()

// 状态
const loading = ref(false)
const activeTab = ref('all')
const messages = ref<MessageVO[]>([])
const total = ref(0)
const totalUnread = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 消息类型映射
const typeMap: Record<string, number | undefined> = {
  all: undefined,
  system: 0,
  match: 1,
  comment: 2
}

// 获取消息图标
const getMessageIcon = (type: number) => {
  switch (type) {
    case 0: return Notification  // 系统通知
    case 1: return Connection    // 匹配通知
    case 2: return ChatDotRound  // 留言通知
    default: return Bell
  }
}

// 获取消息图标样式类
const getMessageIconClass = (type: number) => {
  switch (type) {
    case 0: return 'icon-system'
    case 1: return 'icon-match'
    case 2: return 'icon-comment'
    default: return 'icon-default'
  }
}

// 获取消息标签类型
const getMessageTagType = (type: number): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
  switch (type) {
    case 0: return 'info'     // 系统通知
    case 1: return 'success'  // 匹配通知
    case 2: return 'warning'  // 留言通知
    default: return 'info'
  }
}

// 截断内容
const truncateContent = (content: string, maxLength: number = 100) => {
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }
  // 小于24小时
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }
  // 小于7天
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }
  // 其他情况显示完整日期
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取消息列表
const fetchMessages = async () => {
  loading.value = true
  try {
    const params = {
      type: typeMap[activeTab.value],
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const response = await messageApi.getMessageList(params)
    if (response.code === 200 && response.data) {
      messages.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取消息列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取消息列表失败')
  } finally {
    loading.value = false
  }
}

// 获取未读消息数量
const fetchUnreadCount = async () => {
  try {
    const response = await messageApi.getUnreadCount()
    if (response.code === 200) {
      totalUnread.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

// Tab切换
const handleTabChange = () => {
  pageNum.value = 1
  fetchMessages()
}

// 分页大小变化
const handleSizeChange = () => {
  pageNum.value = 1
  fetchMessages()
}

// 页码变化
const handlePageChange = () => {
  fetchMessages()
}

// 点击消息
const handleMessageClick = async (message: MessageVO) => {
  // 标记已读
  if (!message.isRead) {
    await markMessageRead(message.id)
  }
  
  // 根据消息类型跳转
  if (message.relatedId) {
    switch (message.type) {
      case 1: // 匹配通知 - 跳转到物品详情
        router.push(`/item/${message.relatedId}`)
        break
      case 2: // 留言通知 - 跳转到物品详情
        router.push(`/item/${message.relatedId}`)
        break
      default:
        // 系统通知不跳转
        break
    }
  }
}

// 标记单条消息已读
const handleMarkRead = async (message: MessageVO) => {
  await markMessageRead(message.id)
}

// 标记消息已读
const markMessageRead = async (id: number) => {
  try {
    const response = await messageApi.markMessageAsRead(id)
    if (response.code === 200) {
      // 更新本地状态
      const msg = messages.value.find(m => m.id === id)
      if (msg && !msg.isRead) {
        msg.isRead = true
        totalUnread.value = Math.max(0, totalUnread.value - 1)
      }
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 全部标记已读
const handleMarkAllRead = async () => {
  try {
    const response = await messageApi.markAllMessagesAsRead()
    if (response.code === 200) {
      ElMessage.success('已全部标记为已读')
      // 更新本地状态
      messages.value.forEach(msg => {
        msg.isRead = true
      })
      totalUnread.value = 0
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  fetchMessages()
  fetchUnreadCount()
})
</script>


<style scoped lang="scss">
.messages-page {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background: #f5f7fa;

  .container {
    max-width: 900px;
    margin: 0 auto;
  }
}

.page-header {
  margin-bottom: 20px;

  h1 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  p {
    color: #909399;
    font-size: 14px;
    margin: 0;
  }
}

.messages-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 20px 0;
  border-bottom: 1px solid #ebeef5;

  :deep(.el-tabs) {
    flex: 1;
    
    .el-tabs__header {
      margin: 0;
    }
    
    .el-tabs__nav-wrap::after {
      display: none;
    }
    
    .el-tabs__item {
      padding: 0 20px;
      height: 48px;
      line-height: 48px;
    }
  }

  .tab-badge {
    margin-left: 6px;
    
    :deep(.el-badge__content) {
      transform: translateY(-2px);
    }
  }

  .actions {
    padding-top: 8px;
  }
}

.message-list {
  min-height: 300px;
  padding: 0;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f5f7fa;
  }

  &:last-child {
    border-bottom: none;
  }

  &.unread {
    background-color: #ecf5ff;

    &:hover {
      background-color: #d9ecff;
    }

    .message-title {
      font-weight: 600;
    }
  }
}

.message-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  background-color: #f0f2f5;

  .icon-system {
    color: #909399;
  }

  .icon-match {
    color: #67c23a;
  }

  .icon-comment {
    color: #e6a23c;
  }

  .icon-default {
    color: #409eff;
  }
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.message-title {
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-type {
  flex-shrink: 0;
}

.message-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
  word-break: break-word;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;

  .el-icon {
    font-size: 14px;
  }
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.message-actions {
  flex-shrink: 0;
  margin-left: 16px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

// 响应式设计
@media (max-width: 768px) {
  .messages-page {
    padding: 12px;
  }

  .toolbar {
    flex-direction: column;
    padding: 12px 16px 0;

    :deep(.el-tabs) {
      width: 100%;
      
      .el-tabs__item {
        padding: 0 12px;
        font-size: 14px;
      }
    }

    .actions {
      width: 100%;
      padding: 12px 0;
      display: flex;
      justify-content: flex-end;
    }
  }

  .message-item {
    padding: 12px 16px;
  }

  .message-icon {
    width: 40px;
    height: 40px;
    margin-right: 12px;

    .el-icon {
      font-size: 20px !important;
    }
  }

  .message-title {
    font-size: 14px;
  }

  .message-body {
    font-size: 13px;
  }

  .message-actions {
    display: none;
  }
}
</style>
