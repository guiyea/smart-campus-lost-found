<template>
  <div class="item-detail-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-empty :description="error">
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </el-empty>
    </div>

    <!-- 物品详情内容 -->
    <div v-else-if="item" class="detail-content">
      <!-- 返回按钮 -->
      <div class="back-header">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      </div>

      <el-row :gutter="24">
        <!-- 左侧：图片和基本信息 -->
        <el-col :xs="24" :sm="24" :md="14" :lg="14">
          <!-- 图片轮播 -->
          <el-card class="image-card" shadow="never">
            <el-carousel
              v-if="item.images && item.images.length > 0"
              :autoplay="false"
              indicator-position="outside"
              height="400px"
              class="image-carousel"
            >
              <el-carousel-item v-for="(image, index) in item.images" :key="index">
                <el-image
                  :src="image"
                  fit="contain"
                  class="carousel-image"
                  :preview-src-list="item.images"
                  :initial-index="index"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                </el-image>
              </el-carousel-item>
            </el-carousel>
            <div v-else class="no-image">
              <el-icon><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </el-card>

          <!-- 基本信息卡片 -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="title-row">
                  <h1 class="item-title">{{ item.title }}</h1>
                  <div class="tags">
                    <el-tag
                      :type="item.type === 0 ? 'danger' : 'success'"
                      effect="dark"
                      size="large"
                    >
                      {{ item.type === 0 ? '失物' : '招领' }}
                    </el-tag>
                    <el-tag :type="statusType" size="large">
                      {{ statusText }}
                    </el-tag>
                  </div>
                </div>
                <div class="category-row">
                  <el-tag type="info" effect="plain">
                    {{ item.category || '未分类' }}
                  </el-tag>
                </div>
              </div>
            </template>

            <!-- 描述内容 -->
            <div class="description-section">
              <h3 class="section-title">详细描述</h3>
              <p class="description-text">{{ item.description }}</p>
            </div>

            <!-- 时间地点信息 -->
            <div class="location-time-section">
              <h3 class="section-title">时间地点</h3>
              <div class="info-list">
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span class="label">{{ item.type === 0 ? '丢失时间' : '拾获时间' }}：</span>
                  <span class="value">{{ formatEventTime }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Location /></el-icon>
                  <span class="label">{{ item.type === 0 ? '丢失地点' : '拾获地点' }}：</span>
                  <span class="value">{{ item.locationDesc || '未知地点' }}</span>
                </div>
              </div>

              <!-- 地图展示 -->
              <div v-if="item.longitude && item.latitude" class="map-section">
                <div ref="mapContainer" class="mini-map"></div>
              </div>
            </div>

            <!-- AI识别标签 -->
            <div v-if="item.tags && item.tags.length > 0" class="tags-section">
              <h3 class="section-title">
                <el-icon><MagicStick /></el-icon>
                AI识别标签
              </h3>
              <div class="ai-tags">
                <el-tag
                  v-for="tag in item.tags"
                  :key="tag"
                  type="warning"
                  effect="plain"
                  class="ai-tag"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>

            <!-- 浏览次数 -->
            <div class="stats-section">
              <span class="view-count">
                <el-icon><View /></el-icon>
                {{ item.viewCount || 0 }} 次浏览
              </span>
              <span class="publish-time">
                发布于 {{ formatCreatedAt }}
              </span>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：发布者信息和操作 -->
        <el-col :xs="24" :sm="24" :md="10" :lg="10">
          <!-- 发布者信息 -->
          <el-card class="publisher-card" shadow="never">
            <template #header>
              <span class="card-title">发布者信息</span>
            </template>
            <div class="publisher-info">
              <el-avatar :size="64" :src="item.userAvatar">
                <el-icon :size="32"><User /></el-icon>
              </el-avatar>
              <div class="publisher-details">
                <span class="publisher-name">{{ item.userName || '匿名用户' }}</span>
                <span class="publisher-id">ID: {{ item.userId }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button
                v-if="!isOwner"
                type="primary"
                size="large"
                :icon="ChatDotRound"
                @click="handleContact"
              >
                联系发布者
              </el-button>
              <el-button
                v-if="isOwner"
                type="primary"
                size="large"
                :icon="Edit"
                @click="handleEdit"
              >
                编辑信息
              </el-button>
              <el-button
                v-if="isOwner && item.status === 0"
                type="danger"
                size="large"
                :icon="Delete"
                @click="handleDelete"
              >
                删除信息
              </el-button>
              <el-button
                v-if="showConfirmMatch"
                type="success"
                size="large"
                :icon="Check"
                :loading="confirmingMatch"
                @click="handleConfirmMatch"
              >
                确认匹配
              </el-button>
            </div>
          </el-card>

          <!-- 匹配推荐列表 -->
          <el-card
            v-if="item.matchRecommendations && item.matchRecommendations.length > 0"
            class="recommendations-card"
            shadow="never"
          >
            <template #header>
              <div class="recommendations-header">
                <span class="card-title">
                  <el-icon><Connection /></el-icon>
                  可能匹配的物品
                </span>
                <el-tag type="info" size="small">
                  {{ item.matchRecommendations.length }} 条
                </el-tag>
              </div>
            </template>
            <div class="recommendations-list">
              <div
                v-for="rec in item.matchRecommendations"
                :key="rec.id"
                class="recommendation-item"
              >
                <div class="rec-content" @click="goToDetail(rec.id)">
                  <el-image
                    :src="rec.images?.[0] || defaultImage"
                    fit="cover"
                    class="rec-image"
                  >
                    <template #error>
                      <div class="rec-image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="rec-info">
                    <span class="rec-title">{{ rec.title }}</span>
                    <span class="rec-location">
                      <el-icon><Location /></el-icon>
                      {{ rec.locationDesc || '未知地点' }}
                    </span>
                    <div class="rec-footer">
                      <el-tag
                        :type="rec.type === 0 ? 'danger' : 'success'"
                        size="small"
                      >
                        {{ rec.type === 0 ? '失物' : '招领' }}
                      </el-tag>
                    </div>
                  </div>
                  <el-icon class="rec-arrow"><ArrowRight /></el-icon>
                </div>
                <!-- 匹配度分数显示 -->
                <div class="rec-match-info">
                  <div class="match-score-bar">
                    <span class="match-score-label">匹配度</span>
                    <el-progress
                      :percentage="getMatchScore(rec)"
                      :color="getMatchScoreColor(getMatchScore(rec))"
                      :stroke-width="8"
                      :show-text="false"
                      class="match-progress"
                    />
                    <span class="match-score-value">{{ getMatchScore(rec) }}%</span>
                  </div>
                  <!-- 确认匹配按钮 -->
                  <el-button
                    v-if="canConfirmMatch(rec)"
                    type="success"
                    size="small"
                    :icon="Check"
                    :loading="confirmingMatchId === rec.id"
                    @click.stop="handleConfirmMatchWithItem(rec.id)"
                  >
                    确认匹配
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 联系发布者对话框 -->
    <el-dialog
      v-model="contactDialogVisible"
      title="联系发布者"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="contactForm" label-width="80px">
        <el-form-item label="留言内容">
          <el-input
            v-model="contactForm.message"
            type="textarea"
            :rows="4"
            placeholder="请输入您想对发布者说的话..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendingMessage" @click="sendContactMessage">
          发送
        </el-button>
      </template>
    </el-dialog>

    <!-- 匹配确认弹窗 -->
    <ConfirmMatchDialog
      v-model="confirmMatchDialogVisible"
      :current-item="item"
      :matched-item="selectedMatchedItem"
      :match-score="selectedMatchScore"
      @confirmed="handleMatchConfirmed"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Picture,
  Clock,
  Location,
  View,
  User,
  ChatDotRound,
  Edit,
  Delete,
  Check,
  Connection,
  MagicStick
} from '@element-plus/icons-vue'
import { getItemDetail, deleteItem } from '@/api/item'
import { confirmMatch } from '@/api/match'
import { sendMessage } from '@/api/message'
import { useUserStore } from '@/stores'
import ConfirmMatchDialog from '@/components/match/ConfirmMatchDialog.vue'
import type { ItemDetailVO, ItemVO, MatchVO } from '@/types'

// 声明高德地图类型
declare const AMap: any

// Props
const props = defineProps<{
  id?: string | number
}>()

// Router
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(true)
const error = ref('')
const item = ref<ItemDetailVO | null>(null)
const contactDialogVisible = ref(false)
const sendingMessage = ref(false)
const confirmingMatch = ref(false)
const confirmingMatchId = ref<number | null>(null)
const contactForm = ref({
  message: ''
})

// 匹配确认弹窗状态
const confirmMatchDialogVisible = ref(false)
const selectedMatchedItem = ref<ItemVO | MatchVO | null>(null)
const selectedMatchScore = ref<number | undefined>(undefined)

// 地图相关
const mapContainer = ref<HTMLDivElement | null>(null)
let map: any = null
let marker: any = null

// 默认图片
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjgwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9IiNmNWY3ZmEiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjEyIiBmaWxsPSIjYzBjNGNjIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5pqC5peg5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='

// 计算属性
const itemId = computed(() => {
  return props.id || route.params.id
})

const isOwner = computed(() => {
  return userStore.userInfo?.id === item.value?.userId
})

const showConfirmMatch = computed(() => {
  // 从匹配推荐页面跳转来的，且当前用户是相关物品的发布者
  const matchedItemId = route.query.matchedItemId
  return matchedItemId && item.value?.status === 0 && userStore.isLoggedIn
})

const statusType = computed(() => {
  switch (item.value?.status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'info'
  }
})

const statusText = computed(() => {
  switch (item.value?.status) {
    case 0: return '待处理'
    case 1: return '已找回'
    case 2: return '已关闭'
    default: return '未知'
  }
})

const formatEventTime = computed(() => {
  if (!item.value?.eventTime) return '未知时间'
  const date = new Date(item.value.eventTime)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const formatCreatedAt = computed(() => {
  if (!item.value?.createdAt) return ''
  const date = new Date(item.value.createdAt)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
})

// 方法
const fetchItemDetail = async () => {
  if (!itemId.value) {
    error.value = '物品ID无效'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await getItemDetail(Number(itemId.value))
    if (response.code === 200 && response.data) {
      item.value = response.data
      // 初始化地图
      if (item.value.longitude && item.value.latitude) {
        setTimeout(() => initMap(), 100)
      }
    } else {
      error.value = response.message || '获取物品详情失败'
    }
  } catch (err: any) {
    console.error('Failed to fetch item detail:', err)
    error.value = err.message || '获取物品详情失败'
  } finally {
    loading.value = false
  }
}

const initMap = () => {
  if (!mapContainer.value || !item.value?.longitude || !item.value?.latitude) return

  if (typeof AMap === 'undefined') {
    console.error('高德地图API未加载')
    return
  }

  // 创建地图实例
  map = new AMap.Map(mapContainer.value, {
    zoom: 15,
    center: [item.value.longitude, item.value.latitude],
    resizeEnable: true
  })

  // 添加标记
  const iconConfig = item.value.type === 0
    ? { color: '#F56C6C' }
    : { color: '#67C23A' }

  marker = new AMap.Marker({
    position: [item.value.longitude, item.value.latitude],
    icon: new AMap.Icon({
      size: new AMap.Size(32, 40),
      image: `data:image/svg+xml;base64,${btoa(`<svg width="32" height="40" xmlns="http://www.w3.org/2000/svg"><path d="M16 0 C24.837 0 32 7.163 32 16 C32 28 16 40 16 40 C16 40 0 28 0 16 C0 7.163 7.163 0 16 0 Z" fill="${iconConfig.color}"/><circle cx="16" cy="14" r="6" fill="white"/></svg>`)}`,
      imageSize: new AMap.Size(32, 40)
    }),
    offset: new AMap.Pixel(-16, -40)
  })

  marker.setMap(map)
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push({ name: 'Items' })
  }
}

const goToDetail = (id: number) => {
  router.push({ name: 'ItemDetail', params: { id } })
}

const handleContact = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  contactDialogVisible.value = true
}

const sendContactMessage = async () => {
  if (!contactForm.value.message.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }

  if (!item.value) return

  sendingMessage.value = true
  try {
    await sendMessage({
      userId: item.value.userId,
      title: `关于「${item.value.title}」的留言`,
      content: contactForm.value.message,
      type: 2, // 留言通知
      relatedId: item.value.id
    })
    ElMessage.success('留言发送成功')
    contactDialogVisible.value = false
    contactForm.value.message = ''
  } catch (err: any) {
    ElMessage.error(err.message || '发送失败')
  } finally {
    sendingMessage.value = false
  }
}

const handleEdit = () => {
  router.push({ name: 'ItemEdit', params: { id: itemId.value } })
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条信息吗？删除后将无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteItem(Number(itemId.value))
    if (response.code === 200) {
      ElMessage.success('删除成功')
      router.push({ name: 'Items' })
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

const handleConfirmMatch = async () => {
  const matchedItemId = route.query.matchedItemId

  if (!matchedItemId || !itemId.value) {
    ElMessage.warning('匹配信息不完整')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认这两个物品匹配成功吗？确认后双方物品状态将更新为"已找回"。',
      '确认匹配',
      {
        confirmButtonText: '确认匹配',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    confirmingMatch.value = true
    const response = await confirmMatch({
      itemId: Number(itemId.value),
      matchedItemId: Number(matchedItemId)
    })

    if (response.code === 200) {
      ElMessage.success('匹配确认成功！恭喜找回物品！')
      // 刷新页面数据
      await fetchItemDetail()
    } else {
      ElMessage.error(response.message || '确认匹配失败')
    }
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '确认匹配失败')
    }
  } finally {
    confirmingMatch.value = false
  }
}

// 获取匹配分数
const getMatchScore = (rec: any): number => {
  if (rec.matchScore !== undefined && rec.matchScore !== null) {
    return Math.round(rec.matchScore)
  }
  return 0
}

// 根据匹配分数获取颜色
const getMatchScoreColor = (score: number): string => {
  if (score >= 80) return '#67C23A' // 绿色 - 高匹配度
  if (score >= 60) return '#E6A23C' // 橙色 - 中等匹配度
  if (score >= 40) return '#F56C6C' // 红色 - 低匹配度
  return '#909399' // 灰色 - 很低匹配度
}

// 判断是否可以确认匹配
const canConfirmMatch = (rec: any): boolean => {
  // 用户必须登录
  if (!userStore.isLoggedIn) return false
  // 当前物品必须是待处理状态
  if (item.value?.status !== 0) return false
  // 推荐物品必须是待处理状态
  if (rec.status !== 0) return false
  // 当前用户必须是当前物品或推荐物品的发布者
  const currentUserId = userStore.userInfo?.id
  return currentUserId === item.value?.userId || currentUserId === rec.userId
}

// 确认与推荐物品的匹配
const handleConfirmMatchWithItem = async (matchedItemId: number) => {
  if (!itemId.value || !item.value) {
    ElMessage.warning('物品信息不完整')
    return
  }

  // 找到匹配的物品信息
  const matchedItem = item.value.matchRecommendations?.find(rec => rec.id === matchedItemId)
  if (!matchedItem) {
    ElMessage.warning('未找到匹配物品信息')
    return
  }

  // 设置选中的匹配物品并打开弹窗
  selectedMatchedItem.value = matchedItem
  selectedMatchScore.value = getMatchScore(matchedItem)
  confirmMatchDialogVisible.value = true
}

// 匹配确认成功后的回调
const handleMatchConfirmed = async () => {
  // 刷新页面数据
  await fetchItemDetail()
  // 清空选中状态
  selectedMatchedItem.value = null
  selectedMatchScore.value = undefined
}

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchItemDetail()
    }
  }
)

// 生命周期
onMounted(() => {
  fetchItemDetail()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})
</script>


<style scoped lang="scss">
.item-detail-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container,
.error-container {
  padding: 60px 20px;
  text-align: center;
}

.back-header {
  margin-bottom: 20px;
}

// 图片卡片
.image-card {
  margin-bottom: 20px;
  border-radius: 12px;

  :deep(.el-card__body) {
    padding: 0;
  }
}

.image-carousel {
  border-radius: 12px;
  overflow: hidden;

  :deep(.el-carousel__container) {
    background: #f5f7fa;
  }

  :deep(.el-carousel__indicators) {
    padding: 12px 0;
  }
}

.carousel-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;

  :deep(img) {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
}

.image-error,
.no-image {
  width: 100%;
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;

  .el-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  span {
    font-size: 14px;
  }
}

// 信息卡片
.info-card {
  margin-bottom: 20px;
  border-radius: 12px;

  :deep(.el-card__header) {
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.card-header {
  .title-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 12px;
  }

  .item-title {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    flex: 1;
    line-height: 1.4;
  }

  .tags {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }

  .category-row {
    margin-top: 8px;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: #409eff;
  }
}

.description-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;

  .description-text {
    font-size: 14px;
    line-height: 1.8;
    color: #606266;
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

.location-time-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;

  .info-list {
    margin-bottom: 16px;
  }

  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 14px;

    .el-icon {
      color: #909399;
      flex-shrink: 0;
    }

    .label {
      color: #909399;
      flex-shrink: 0;
    }

    .value {
      color: #303133;
    }
  }
}

.map-section {
  margin-top: 16px;
}

.mini-map {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.tags-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;

  .ai-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .ai-tag {
    font-size: 13px;
  }
}

.stats-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;

  .view-count {
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      font-size: 16px;
    }
  }
}

// 发布者卡片
.publisher-card {
  margin-bottom: 20px;
  border-radius: 12px;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  .publisher-details {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .publisher-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .publisher-id {
    font-size: 13px;
    color: #909399;
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .el-button {
    width: 100%;
  }
}

// 推荐卡片
.recommendations-card {
  border-radius: 12px;

  .recommendations-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        color: #409eff;
      }
    }
  }
}

.recommendations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommendation-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    background: #f0f0f0;
  }

  .rec-content {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateX(4px);
    }
  }

  .rec-image {
    width: 60px;
    height: 60px;
    border-radius: 6px;
    flex-shrink: 0;
    overflow: hidden;
  }

  .rec-image-error {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;
  }

  .rec-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .rec-title {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .rec-location {
    font-size: 12px;
    color: #909399;
    display: flex;
    align-items: center;
    gap: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .el-icon {
      flex-shrink: 0;
    }
  }

  .rec-footer {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .rec-arrow {
    color: #c0c4cc;
    flex-shrink: 0;
  }
}

.rec-match-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px dashed #e4e7ed;

  .match-score-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    min-width: 0;
  }

  .match-score-label {
    font-size: 12px;
    color: #909399;
    flex-shrink: 0;
  }

  .match-progress {
    flex: 1;
    min-width: 60px;
    max-width: 120px;
  }

  .match-score-value {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    flex-shrink: 0;
    min-width: 40px;
    text-align: right;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .item-detail-page {
    padding: 12px;
  }

  .card-header {
    .title-row {
      flex-direction: column;
      gap: 12px;
    }

    .item-title {
      font-size: 18px;
    }

    .tags {
      align-self: flex-start;
    }
  }

  .no-image,
  .image-error {
    height: 250px;
  }

  .image-carousel {
    :deep(.el-carousel__container) {
      height: 250px !important;
    }
  }

  .publisher-card,
  .recommendations-card {
    margin-top: 20px;
  }

  .recommendation-item {
    .rec-image {
      width: 50px;
      height: 50px;
    }
  }

  .rec-match-info {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;

    .match-score-bar {
      justify-content: space-between;
    }

    .match-progress {
      max-width: none;
    }

    .el-button {
      width: 100%;
    }
  }
}
</style>
