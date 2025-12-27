<template>
  <el-card class="item-card" shadow="hover" @click="handleClick">
    <!-- 物品图片 -->
    <div class="item-image">
      <el-image
        :src="coverImage"
        fit="cover"
        class="image"
        lazy
      >
        <template #placeholder>
          <div class="image-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
        </template>
        <template #error>
          <div class="image-placeholder">
            <el-icon><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </template>
      </el-image>

      <!-- 类型标签 -->
      <el-tag
        :type="item.type === 0 ? 'danger' : 'success'"
        class="type-tag"
        size="small"
        effect="dark"
      >
        {{ item.type === 0 ? '失物' : '招领' }}
      </el-tag>

      <!-- 状态标签 -->
      <el-tag
        :type="statusType"
        class="status-tag"
        size="small"
      >
        {{ statusText }}
      </el-tag>
    </div>

    <!-- 物品信息 -->
    <div class="item-content">
      <!-- 标题 -->
      <h3 class="item-title" :title="item.title">{{ item.title }}</h3>

      <!-- 类别标签 -->
      <div class="item-category">
        <el-tag size="small" type="info" effect="plain">
          {{ item.category || '未分类' }}
        </el-tag>
      </div>

      <!-- 地点 -->
      <div class="item-location">
        <el-icon><Location /></el-icon>
        <span :title="item.locationDesc">{{ item.locationDesc || '未知地点' }}</span>
      </div>

      <!-- 时间 -->
      <div class="item-time">
        <el-icon><Clock /></el-icon>
        <span>{{ formatEventTime }}</span>
      </div>

      <!-- 底部信息 -->
      <div class="item-footer">
        <div class="item-stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ item.viewCount || 0 }}
          </span>
        </div>
        <div class="item-date">
          {{ formatCreatedAt }}
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Location, Clock, View, Picture, Loading } from '@element-plus/icons-vue'
import type { ItemVO } from '@/types'

// Props
const props = defineProps<{
  item: ItemVO
}>()

// Emits
const emit = defineEmits<{
  (e: 'click', item: ItemVO): void
}>()

// 默认图片
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmN2ZhIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iI2MwYzRjYyIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuaaguaXoOWbvueJhzwvdGV4dD48L3N2Zz4='

// 封面图片
const coverImage = computed(() => {
  if (props.item.images && props.item.images.length > 0) {
    return props.item.images[0]
  }
  return defaultImage
})

// 状态类型
const statusType = computed(() => {
  switch (props.item.status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'info'
    default:
      return 'info'
  }
})

// 状态文本
const statusText = computed(() => {
  switch (props.item.status) {
    case 0:
      return '待处理'
    case 1:
      return '已找回'
    case 2:
      return '已关闭'
    default:
      return '未知'
  }
})

// 格式化事件时间
const formatEventTime = computed(() => {
  if (!props.item.eventTime) return '未知时间'
  const date = new Date(props.item.eventTime)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
})

// 格式化创建时间
const formatCreatedAt = computed(() => {
  if (!props.item.createdAt) return ''
  const date = new Date(props.item.createdAt)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit'
    })
  }
})

// 点击处理
const handleClick = () => {
  emit('click', props.item)
}
</script>

<style scoped lang="scss">
.item-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-radius: 8px;
  overflow: hidden;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.item-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;

  .image {
    width: 100%;
    height: 100%;
    display: block;
  }

  .image-loading,
  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;

    .el-icon {
      font-size: 32px;
      margin-bottom: 8px;
    }

    span {
      font-size: 12px;
    }
  }

  .type-tag {
    position: absolute;
    top: 10px;
    left: 10px;
    border-radius: 4px;
  }

  .status-tag {
    position: absolute;
    top: 10px;
    right: 10px;
    border-radius: 4px;
  }
}

.item-content {
  padding: 14px;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.item-category {
  margin-bottom: 10px;

  .el-tag {
    font-size: 11px;
  }
}

.item-location,
.item-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
  line-height: 1.4;

  .el-icon {
    flex-shrink: 0;
    font-size: 14px;
  }

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.item-stats {
  display: flex;
  gap: 12px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #909399;

    .el-icon {
      font-size: 14px;
    }
  }
}

.item-date {
  font-size: 12px;
  color: #c0c4cc;
}

// 响应式设计
@media (max-width: 768px) {
  .item-image {
    height: 160px;
  }

  .item-content {
    padding: 12px;
  }

  .item-title {
    font-size: 14px;
  }

  .item-location,
  .item-time {
    font-size: 12px;
  }
}
</style>
