<template>
  <el-dialog
    v-model="dialogVisible"
    title="确认匹配"
    width="700px"
    :close-on-click-modal="false"
    :close-on-press-escape="!confirming"
    :show-close="!confirming"
    class="confirm-match-dialog"
    @closed="handleClosed"
  >
    <!-- 对比信息区域 -->
    <div class="comparison-container">
      <!-- 当前物品 -->
      <div class="item-card">
        <div class="item-header">
          <el-tag :type="currentItem?.type === 0 ? 'danger' : 'success'" effect="dark">
            {{ currentItem?.type === 0 ? '失物' : '招领' }}
          </el-tag>
          <span class="item-label">当前物品</span>
        </div>
        <div class="item-content">
          <el-image
            :src="currentItem?.images?.[0] || defaultImage"
            fit="cover"
            class="item-image"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="item-info">
            <h4 class="item-title">{{ currentItem?.title || '未知标题' }}</h4>
            <p class="item-desc">{{ truncateText(currentItem?.description, 60) }}</p>
            <div class="item-meta">
              <span class="meta-item">
                <el-icon><Location /></el-icon>
                {{ currentItem?.locationDesc || '未知地点' }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatDate(currentItem?.eventTime) }}
              </span>
            </div>
            <div class="item-category">
              <el-tag type="info" size="small">{{ currentItem?.category || '未分类' }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 匹配图标 -->
      <div class="match-icon">
        <el-icon :size="32" color="#67C23A"><Connection /></el-icon>
        <span class="match-text">匹配</span>
        <div v-if="matchScore !== undefined" class="match-score">
          <span class="score-value">{{ matchScore }}%</span>
          <span class="score-label">匹配度</span>
        </div>
      </div>

      <!-- 匹配物品 -->
      <div class="item-card">
        <div class="item-header">
          <el-tag :type="matchedItem?.type === 0 ? 'danger' : 'success'" effect="dark">
            {{ matchedItem?.type === 0 ? '失物' : '招领' }}
          </el-tag>
          <span class="item-label">匹配物品</span>
        </div>
        <div class="item-content">
          <el-image
            :src="matchedItem?.images?.[0] || defaultImage"
            fit="cover"
            class="item-image"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="item-info">
            <h4 class="item-title">{{ matchedItem?.title || '未知标题' }}</h4>
            <p class="item-desc">{{ truncateText(matchedItem?.description, 60) }}</p>
            <div class="item-meta">
              <span class="meta-item">
                <el-icon><Location /></el-icon>
                {{ matchedItem?.locationDesc || '未知地点' }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatDate(matchedItem?.eventTime) }}
              </span>
            </div>
            <div class="item-category">
              <el-tag type="info" size="small">{{ matchedItem?.category || '未分类' }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示信息 -->
    <div class="confirm-tips">
      <el-alert
        title="确认匹配后，双方物品状态将更新为【已找回】，招领方将获得50积分奖励。"
        type="info"
        :closable="false"
        show-icon
      />
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button :disabled="confirming" @click="handleCancel">取消</el-button>
        <el-button
          type="success"
          :loading="confirming"
          :icon="Check"
          @click="handleConfirm"
        >
          {{ confirming ? '确认中...' : '确认匹配' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Location, Clock, Connection, Check } from '@element-plus/icons-vue'
import { confirmMatch } from '@/api/match'
import type { ItemVO, MatchVO } from '@/types'

// Props
interface Props {
  /** 控制弹窗显示 */
  modelValue: boolean
  /** 当前物品信息 */
  currentItem?: ItemVO | null
  /** 匹配物品信息 */
  matchedItem?: ItemVO | MatchVO | null
  /** 匹配分数（可选） */
  matchScore?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  currentItem: null,
  matchedItem: null,
  matchScore: undefined
})

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirmed'): void
  (e: 'cancelled'): void
}>()

// 状态
const confirming = ref(false)

// 默认图片
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjgwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9IiNmNWY3ZmEiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjEyIiBmaWxsPSIjYzBjNGNjIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5pqC5peg5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
const truncateText = (text: string | undefined, maxLength: number): string => {
  if (!text) return '暂无描述'
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

const formatDate = (dateStr: string | undefined): string => {
  if (!dateStr) return '未知时间'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch {
    return '未知时间'
  }
}

const handleCancel = () => {
  dialogVisible.value = false
  emit('cancelled')
}

const handleClosed = () => {
  confirming.value = false
}

const handleConfirm = async () => {
  if (!props.currentItem?.id || !props.matchedItem?.id) {
    ElMessage.error('物品信息不完整，无法确认匹配')
    return
  }

  confirming.value = true

  try {
    const response = await confirmMatch({
      itemId: props.currentItem.id,
      matchedItemId: props.matchedItem.id
    })

    if (response.code === 200) {
      ElMessage.success('匹配确认成功！恭喜找回物品！')
      dialogVisible.value = false
      emit('confirmed')
    } else {
      ElMessage.error(response.message || '确认匹配失败')
    }
  } catch (err: any) {
    console.error('Confirm match failed:', err)
    ElMessage.error(err.message || '确认匹配失败，请稍后重试')
  } finally {
    confirming.value = false
  }
}
</script>

<style scoped lang="scss">
.confirm-match-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;
    margin-right: 0;
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #f0f0f0;
  }
}

.comparison-container {
  display: flex;
  align-items: stretch;
  gap: 16px;
}

.item-card {
  flex: 1;
  background: #f9f9f9;
  border-radius: 12px;
  padding: 16px;
  min-width: 0;

  .item-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .item-label {
      font-size: 12px;
      color: #909399;
    }
  }

  .item-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .item-image {
    width: 100%;
    height: 120px;
    border-radius: 8px;
    overflow: hidden;
    background: #f5f7fa;
  }

  .image-error {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;

    .el-icon {
      font-size: 32px;
    }
  }

  .item-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .item-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .item-desc {
    font-size: 12px;
    color: #606266;
    margin: 0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .item-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #909399;

      .el-icon {
        font-size: 14px;
        flex-shrink: 0;
      }
    }
  }

  .item-category {
    margin-top: 4px;
  }
}

.match-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  flex-shrink: 0;

  .match-text {
    font-size: 12px;
    color: #67C23A;
    margin-top: 4px;
    font-weight: 500;
  }

  .match-score {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 12px;
    padding: 8px 12px;
    background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
    border-radius: 8px;

    .score-value {
      font-size: 18px;
      font-weight: 700;
      color: #fff;
    }

    .score-label {
      font-size: 10px;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

.confirm-tips {
  margin-top: 20px;

  :deep(.el-alert) {
    border-radius: 8px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .confirm-match-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 10px auto !important;
    }
  }

  .comparison-container {
    flex-direction: column;
    gap: 12px;
  }

  .match-icon {
    flex-direction: row;
    gap: 12px;
    padding: 12px 0;

    .match-text {
      margin-top: 0;
    }

    .match-score {
      margin-top: 0;
      margin-left: auto;
    }
  }

  .item-card {
    .item-content {
      flex-direction: row;
      align-items: flex-start;
    }

    .item-image {
      width: 80px;
      height: 80px;
      flex-shrink: 0;
    }

    .item-info {
      flex: 1;
      min-width: 0;
    }
  }
}
</style>
