<template>
  <div class="message-badge-wrapper">
    <el-badge 
      :value="unreadCount" 
      :hidden="unreadCount === 0" 
      :max="99"
      class="message-badge"
    >
      <el-button 
        text 
        class="message-btn"
        @click="handleClick"
      >
        <el-icon :size="iconSize">
          <Bell />
        </el-icon>
      </el-button>
    </el-badge>
  </div>
</template>

<script setup lang="ts">
/**
 * 消息提醒组件
 * 
 * 功能：
 * - 显示在导航栏的消息图标
 * - 显示未读消息数量角标
 * - 点击跳转消息中心
 * - 实时更新未读数量
 * 
 * _Requirements: 6.3_
 */
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import * as messageApi from '@/api/message'
import { useUserStore } from '@/stores'
import { onWebSocketMessage } from '@/utils/websocket'

// Props
interface Props {
  /** 图标大小 */
  iconSize?: number
  /** 轮询间隔（毫秒），0 表示不轮询 */
  pollInterval?: number
}

const props = withDefaults(defineProps<Props>(), {
  iconSize: 20,
  pollInterval: 60000 // 默认 60 秒轮询一次
})

// Emits
const emit = defineEmits<{
  (e: 'update:count', count: number): void
}>()

const router = useRouter()
const userStore = useUserStore()

// 状态
const unreadCount = ref(0)
let pollTimer: ReturnType<typeof setInterval> | null = null
let unsubscribeWs: (() => void) | null = null

// 获取未读消息数量
const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) {
    unreadCount.value = 0
    return
  }

  try {
    const response = await messageApi.getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
      emit('update:count', unreadCount.value)
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

// 处理点击
const handleClick = () => {
  router.push('/messages')
}

// 启动轮询
const startPolling = () => {
  if (props.pollInterval > 0 && !pollTimer) {
    pollTimer = setInterval(fetchUnreadCount, props.pollInterval)
  }
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 监听用户登录状态
watch(
  () => userStore.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn) {
      fetchUnreadCount()
      startPolling()
    } else {
      unreadCount.value = 0
      stopPolling()
    }
  }
)

onMounted(() => {
  // 初始获取未读数量
  fetchUnreadCount()
  
  // 启动轮询
  startPolling()
  
  // 监听 WebSocket 消息，实时更新未读数量
  unsubscribeWs = onWebSocketMessage((message) => {
    // 收到新消息时增加未读数量
    if (message.type === 'message' || message.type === 'match' || message.type === 'system') {
      unreadCount.value++
      emit('update:count', unreadCount.value)
    }
  })
})

onUnmounted(() => {
  stopPolling()
  if (unsubscribeWs) {
    unsubscribeWs()
  }
})

// 暴露方法供外部调用
defineExpose({
  /** 刷新未读数量 */
  refresh: fetchUnreadCount,
  /** 获取当前未读数量 */
  getCount: () => unreadCount.value
})
</script>

<style scoped lang="scss">
.message-badge-wrapper {
  display: inline-flex;
  align-items: center;
}

.message-badge {
  :deep(.el-badge__content) {
    transform: translateY(-4px) translateX(4px);
  }
}

.message-btn {
  padding: 8px;
  font-size: inherit;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
    border-radius: 50%;
  }
  
  .el-icon {
    color: #606266;
    transition: color 0.2s;
  }
  
  &:hover .el-icon {
    color: #409eff;
  }
}
</style>
