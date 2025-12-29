<template>
  <div class="home">
    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow">校园失物招领 · 智能匹配</p>
        <h1>帮每一件物品找到回家的路</h1>
        <p class="subtitle">
          发布、查找、匹配、消息提醒一站式完成，实时掌握失物动态。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/publish')">
            <el-icon><Promotion /></el-icon>
            立即发布信息
          </el-button>
          <el-button size="large" @click="router.push('/my/items')" :disabled="!isAuthenticated">
            <el-icon><Collection /></el-icon>
            我的发布
          </el-button>
          <el-button size="large" plain @click="router.push('/items')">
            <el-icon><Compass /></el-icon>
            浏览全部
          </el-button>
        </div>
        <div class="quick-actions">
          <div
            v-for="action in quickActions"
            :key="action.title"
            class="quick-action"
          >
            <el-icon :class="['action-icon', action.type]"><component :is="action.icon" /></el-icon>
            <div class="action-text">
              <p class="action-title">{{ action.title }}</p>
              <p class="action-desc">{{ action.desc }}</p>
            </div>
            <el-button text type="primary" @click="action.onClick">
              {{ action.cta }}
            </el-button>
          </div>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-card" v-for="stat in statCards" :key="stat.label">
          <p class="stat-label">{{ stat.label }}</p>
          <p class="stat-value">{{ stat.value }}</p>
          <p class="stat-hint">{{ stat.hint }}</p>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-header">
        <div>
          <h2>最新信息</h2>
          <p class="section-subtitle">实时掌握校园失物 / 招领动态</p>
        </div>
        <el-button link type="primary" @click="router.push('/items')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <el-card class="list-card" shadow="hover" v-loading="loadingLatest">
            <div class="list-card__header">
              <div class="chip danger">最新失物</div>
              <el-button text type="primary" @click="router.push({ name: 'Items', query: { type: 0 } })">
                更多
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            <el-empty v-if="latestLostItems.length === 0" description="暂无失物信息" />
            <div v-else class="item-list">
              <div v-for="item in latestLostItems" :key="item.id" class="item-row" @click="goToDetail(item.id)">
                <el-image :src="item.images?.[0]" fit="cover" class="item-thumb">
                  <template #error>
                    <div class="thumb-placeholder">No Image</div>
                  </template>
                </el-image>
                <div class="item-meta">
                  <p class="item-title">{{ item.title }}</p>
                  <p class="item-desc">{{ item.description }}</p>
                  <div class="item-info">
                    <span><el-icon><Location /></el-icon>{{ item.locationDesc || '未知地点' }}</span>
                    <span><el-icon><Clock /></el-icon>{{ formatDate(item.eventTime) }}</span>
                  </div>
                </div>
                <el-tag size="small" type="info">失物</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-card class="list-card" shadow="hover" v-loading="loadingLatest">
            <div class="list-card__header">
              <div class="chip success">最新招领</div>
              <el-button text type="primary" @click="router.push({ name: 'Items', query: { type: 1 } })">
                更多
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            <el-empty v-if="latestFoundItems.length === 0" description="暂无招领信息" />
            <div v-else class="item-list">
              <div v-for="item in latestFoundItems" :key="item.id" class="item-row" @click="goToDetail(item.id)">
                <el-image :src="item.images?.[0]" fit="cover" class="item-thumb">
                  <template #error>
                    <div class="thumb-placeholder">No Image</div>
                  </template>
                </el-image>
                <div class="item-meta">
                  <p class="item-title">{{ item.title }}</p>
                  <p class="item-desc">{{ item.description }}</p>
                  <div class="item-info">
                    <span><el-icon><Location /></el-icon>{{ item.locationDesc || '未知地点' }}</span>
                    <span><el-icon><Clock /></el-icon>{{ formatDate(item.eventTime) }}</span>
                  </div>
                </div>
                <el-tag size="small" type="success">招领</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section" v-if="isAuthenticated">
      <div class="section-header">
        <div>
          <h2>为你推荐</h2>
          <p class="section-subtitle">基于你的发布智能匹配</p>
        </div>
        <el-button link type="primary" @click="fetchRecommendations">
          刷新推荐
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      <MatchRecommendationList
        :recommendations="recommendations"
        @refresh="fetchRecommendations"
      />
    </section>

    <section class="section">
      <div class="section-header">
        <div>
          <h2>常用入口</h2>
          <p class="section-subtitle">快速到达常用功能</p>
        </div>
      </div>
      <div class="action-grid">
        <el-card v-for="card in featureCards" :key="card.title" class="action-card" shadow="hover">
          <div class="action-card__header">
            <el-icon :class="['card-icon', card.type]"><component :is="card.icon" /></el-icon>
            <div>
              <p class="card-title">{{ card.title }}</p>
              <p class="card-desc">{{ card.desc }}</p>
            </div>
          </div>
          <el-button type="primary" text @click="card.onClick">
            {{ card.cta }}
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-card>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserMatchRecommendations } from '@/api/match'
import { searchItems } from '@/api/item'
import MatchRecommendationList from '@/components/match/MatchRecommendationList.vue'
import type { ItemVO, MatchVO } from '@/types'
import {
  ArrowRight,
  Clock,
  Collection,
  Compass,
  Location,
  Promotion,
  Refresh,
  Tickets,
  Message,
  Coordinate
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const recommendations = ref<MatchVO[]>([])
const latestLostItems = ref<ItemVO[]>([])
const latestFoundItems = ref<ItemVO[]>([])
const loadingLatest = ref(false)

const stats = ref({
  lost: 0,
  found: 0,
  resolved: 0,
})

const isAuthenticated = computed(() => userStore.isLoggedIn && !userStore.isGuestMode)

const quickActions = [
  {
    title: '我要发布',
    desc: '发布失物/招领信息并支持图片上传、定位',
    cta: '去发布',
    icon: Promotion,
    type: 'primary',
    onClick: () => router.push('/publish'),
  },
  {
    title: '我的消息',
    desc: '查看系统提醒、匹配通知、留言回复',
    cta: '查看消息',
    icon: Message,
    type: 'info',
    onClick: () => router.push('/messages'),
  },
  {
    title: '积分中心',
    desc: '参与招领获得积分，查看排行榜',
    cta: '查看积分',
    icon: Tickets,
    type: 'success',
    onClick: () => router.push('/points'),
  },
]

const statCards = computed(() => [
  { label: '失物信息', value: stats.value.lost, hint: '累计发布失物' },
  { label: '招领信息', value: stats.value.found, hint: '大家拾到的物品' },
  { label: '已归还', value: stats.value.resolved, hint: '成功匹配归还' },
])

const featureCards = [
  {
    title: '快速查找',
    desc: '通过关键词和分类精准搜索',
    icon: Compass,
    type: 'primary',
    cta: '前往查找',
    onClick: () => router.push('/items'),
  },
  {
    title: '地图定位',
    desc: '查看物品丢失/拾获地点',
    icon: Coordinate,
    type: 'info',
    cta: '查看地图',
    onClick: () => router.push({ name: 'Items', query: { view: 'map' } }),
  },
  {
    title: '个人中心',
    desc: '管理个人资料与发布记录',
    icon: Collection,
    type: 'success',
    cta: '打开个人中心',
    onClick: () => router.push('/my/profile'),
  },
]

const goToDetail = (id: number) => {
  router.push(`/item/${id}`)
}

const formatDate = (value: string) => {
  if (!value) return '时间未知'
  const date = new Date(value)
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const fetchRecommendations = async () => {
  if (!isAuthenticated.value) return
  try {
    const response = await getUserMatchRecommendations()
    if (response.data) {
      recommendations.value = response.data
    }
  } catch (error) {
    console.error('Failed to fetch recommendations:', error)
  }
}

const fetchStats = async () => {
  try {
    const [lostRes, foundRes, resolvedRes] = await Promise.all([
      searchItems({ type: 0, pageSize: 1, pageNum: 1 }),
      searchItems({ type: 1, pageSize: 1, pageNum: 1 }),
      searchItems({ status: 1, pageSize: 1, pageNum: 1 }),
    ])
    stats.value.lost = lostRes.data?.total || 0
    stats.value.found = foundRes.data?.total || 0
    stats.value.resolved = resolvedRes.data?.total || 0
  } catch (error: any) {
    console.error('Failed to fetch stats', error)
  }
}

const fetchLatest = async () => {
  loadingLatest.value = true
  try {
    const [lostRes, foundRes] = await Promise.all([
      searchItems({ type: 0, pageSize: 4, pageNum: 1, sortBy: 'time' }),
      searchItems({ type: 1, pageSize: 4, pageNum: 1, sortBy: 'time' }),
    ])
    latestLostItems.value = lostRes.data?.records || []
    latestFoundItems.value = foundRes.data?.records || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取首页数据失败')
  } finally {
    loadingLatest.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchLatest()
  fetchRecommendations()
})
</script>

<style scoped lang="scss">
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.hero {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f9fbff 100%);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.08);
  margin-bottom: 24px;
}

.hero-content h1 {
  font-size: 28px;
  margin: 8px 0 10px;
  color: #303133;
}

.subtitle {
  color: #606266;
  margin-bottom: 16px;
}

.eyebrow {
  color: #409eff;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.quick-action {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.04);
}

.action-icon {
  font-size: 24px;
  padding: 8px;
  border-radius: 10px;
  background: #f5f7fa;
  &.primary { color: #409eff; }
  &.info { color: #909399; }
  &.success { color: #67c23a; }
}

.action-text {
  flex: 1;
}

.action-title {
  margin: 0;
  font-weight: 600;
}

.action-desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.hero-stats {
  display: grid;
  gap: 12px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}

.stat-label {
  margin: 0;
  color: #606266;
}

.stat-value {
  margin: 6px 0 4px;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-hint {
  margin: 0;
  color: #909399;
}

.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-subtitle {
  margin: 4px 0 0;
  color: #909399;
}

.list-card {
  min-height: 260px;
}

.list-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  &.danger { background: #fef0f0; color: #f56c6c; }
  &.success { background: #f0f9eb; color: #67c23a; }
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-row {
  display: grid;
  grid-template-columns: 96px 1fr auto;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  background: #f9fafb;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  }
}

.item-thumb {
  width: 100%;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
}

.thumb-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f2f3f5;
  color: #c0c4cc;
  font-size: 12px;
}

.item-meta {
  overflow: hidden;
}

.item-title {
  margin: 0 0 4px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.item-desc {
  margin: 0 0 6px;
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.item-info {
  display: flex;
  gap: 12px;
  color: #909399;
  font-size: 12px;
  align-items: center;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.action-card__header {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
}

.card-icon {
  font-size: 24px;
  padding: 8px;
  border-radius: 10px;
  background: #f5f7fa;
  &.primary { color: #409eff; }
  &.info { color: #909399; }
  &.success { color: #67c23a; }
}

.card-title {
  margin: 0;
  font-weight: 700;
}

.card-desc {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
