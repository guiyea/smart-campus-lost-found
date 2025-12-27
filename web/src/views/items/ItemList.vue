<template>
  <div class="items-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>失物招领</h1>
        <p>浏览失物和招领信息，帮助物品找到主人</p>
      </div>

      <el-card class="search-card">
        <!-- 搜索栏 -->
        <div class="search-section">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索物品名称、描述..."
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="toggleFilters">
            <el-icon><Filter /></el-icon>
            {{ showFilters ? '收起筛选' : '展开筛选' }}
          </el-button>
        </div>

        <!-- 筛选栏 -->
        <el-collapse-transition>
          <div v-show="showFilters" class="filter-section">
            <el-row :gutter="16">
              <!-- 类型筛选 -->
              <el-col :xs="24" :sm="12" :md="6">
                <div class="filter-item">
                  <span class="filter-label">类型：</span>
                  <el-select
                    v-model="searchForm.type"
                    placeholder="全部类型"
                    clearable
                    class="filter-select"
                  >
                    <el-option label="失物" :value="0" />
                    <el-option label="招领" :value="1" />
                  </el-select>
                </div>
              </el-col>

              <!-- 类别筛选 -->
              <el-col :xs="24" :sm="12" :md="6">
                <div class="filter-item">
                  <span class="filter-label">类别：</span>
                  <el-select
                    v-model="searchForm.category"
                    placeholder="全部类别"
                    clearable
                    class="filter-select"
                  >
                    <el-option
                      v-for="cat in categories"
                      :key="cat"
                      :label="cat"
                      :value="cat"
                    />
                  </el-select>
                </div>
              </el-col>

              <!-- 状态筛选 -->
              <el-col :xs="24" :sm="12" :md="6">
                <div class="filter-item">
                  <span class="filter-label">状态：</span>
                  <el-select
                    v-model="searchForm.status"
                    placeholder="全部状态"
                    clearable
                    class="filter-select"
                  >
                    <el-option label="待处理" :value="0" />
                    <el-option label="已找回" :value="1" />
                    <el-option label="已关闭" :value="2" />
                  </el-select>
                </div>
              </el-col>

              <!-- 时间范围 -->
              <el-col :xs="24" :sm="12" :md="6">
                <div class="filter-item">
                  <span class="filter-label">时间：</span>
                  <el-date-picker
                    v-model="dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="YYYY-MM-DD"
                    class="filter-date"
                    @change="handleDateChange"
                  />
                </div>
              </el-col>
            </el-row>

            <div class="filter-actions">
              <el-button @click="resetFilters">
                <el-icon><RefreshRight /></el-icon>
                重置筛选
              </el-button>
            </div>
          </div>
        </el-collapse-transition>

        <!-- 排序和视图切换 -->
        <div class="toolbar-section">
          <div class="sort-options">
            <span class="toolbar-label">排序：</span>
            <el-radio-group v-model="searchForm.sortBy" @change="handleSearch">
              <el-radio-button label="time">最新发布</el-radio-button>
              <el-radio-button label="distance" :disabled="!hasLocation">
                距离最近
              </el-radio-button>
            </el-radio-group>
          </div>

          <div class="view-toggle">
            <span class="toolbar-label">视图：</span>
            <el-radio-group v-model="viewMode">
              <el-radio-button label="list">
                <el-icon><Grid /></el-icon>
                列表
              </el-radio-button>
              <el-radio-button label="map">
                <el-icon><MapLocation /></el-icon>
                地图
              </el-radio-button>
            </el-radio-group>
          </div>

          <div class="result-count">
            共 <span class="count">{{ total }}</span> 条结果
          </div>
        </div>
      </el-card>

      <!-- 内容区域 -->
      <div class="content-section">
        <!-- 列表视图 -->
        <div v-if="viewMode === 'list'" v-loading="loading" class="list-view">
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
                <ItemCard :item="item" @click="viewDetail(item.id)" />
              </el-col>
            </el-row>

            <!-- 分页 -->
            <div class="pagination-section">
              <el-pagination
                v-model:current-page="searchForm.pageNum"
                v-model:page-size="searchForm.pageSize"
                :page-sizes="[12, 20, 40, 60]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </template>

          <!-- 空状态 -->
          <el-empty v-else description="暂无物品信息">
            <el-button type="primary" @click="goToPublish">
              发布信息
            </el-button>
          </el-empty>
        </div>

        <!-- 地图视图 -->
        <div v-else class="map-view">
          <ItemMap
            :items="items"
            :center="mapCenter"
            @marker-click="handleMarkerClick"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Filter,
  RefreshRight,
  Grid,
  MapLocation
} from '@element-plus/icons-vue'
import * as itemApi from '@/api/item'
import type { ItemVO, ItemSearchDTO } from '@/types'
import ItemCard from '@/components/item/ItemCard.vue'
import ItemMap from '@/components/map/ItemMap.vue'

const router = useRouter()
const route = useRoute()

// 物品类别列表
const categories = [
  '电子设备',
  '证件卡片',
  '钥匙',
  '钱包',
  '书籍文具',
  '衣物配饰',
  '运动器材',
  '其他'
]

// 搜索表单
const searchForm = reactive<ItemSearchDTO>({
  keyword: '',
  type: undefined,
  category: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined,
  sortBy: 'time',
  pageNum: 1,
  pageSize: 12
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 状态
const loading = ref(false)
const showFilters = ref(false)
const viewMode = ref<'list' | 'map'>('list')
const items = ref<ItemVO[]>([])
const total = ref(0)

// 用户位置
const userLocation = ref<{ lng: number; lat: number } | null>(null)

// 地图中心点
const mapCenter = computed(() => {
  if (userLocation.value) {
    return userLocation.value
  }
  // 默认中心点（北京）
  return { lng: 116.397428, lat: 39.90923 }
})

// 是否有位置信息
const hasLocation = computed(() => !!userLocation.value)

// 切换筛选显示
const toggleFilters = () => {
  showFilters.value = !showFilters.value
}

// 处理日期变化
const handleDateChange = (val: [string, string] | null) => {
  if (val) {
    searchForm.startTime = val[0]
    searchForm.endTime = val[1]
  } else {
    searchForm.startTime = undefined
    searchForm.endTime = undefined
  }
}

// 重置筛选
const resetFilters = () => {
  searchForm.keyword = ''
  searchForm.type = undefined
  searchForm.category = undefined
  searchForm.status = undefined
  searchForm.startTime = undefined
  searchForm.endTime = undefined
  searchForm.sortBy = 'time'
  dateRange.value = null
  handleSearch()
}

// 搜索
const handleSearch = async () => {
  searchForm.pageNum = 1
  await fetchItems()
}

// 获取物品列表
const fetchItems = async () => {
  loading.value = true
  try {
    // 如果按距离排序，需要传入位置信息
    const params: ItemSearchDTO = { ...searchForm }
    if (searchForm.sortBy === 'distance' && userLocation.value) {
      params.longitude = userLocation.value.lng
      params.latitude = userLocation.value.lat
    }

    const response = await itemApi.searchItems(params)
    if (response.code === 200 && response.data) {
      items.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

// 分页大小变化
const handleSizeChange = () => {
  searchForm.pageNum = 1
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

// 地图标记点击
const handleMarkerClick = (item: ItemVO) => {
  viewDetail(item.id)
}

// 跳转发布页面
const goToPublish = () => {
  router.push('/publish')
}

// 获取用户位置
const getUserLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        userLocation.value = {
          lng: position.coords.longitude,
          lat: position.coords.latitude
        }
      },
      (error) => {
        console.warn('获取位置失败:', error)
      },
      {
        enableHighAccuracy: true,
        timeout: 10000
      }
    )
  }
}

// 从路由参数初始化筛选条件
const initFromRoute = () => {
  const query = route.query
  if (query.type !== undefined) {
    searchForm.type = Number(query.type)
  }
  if (query.status) {
    // 兼容旧的 status 参数（lost/found）
    if (query.status === 'lost') {
      searchForm.type = 0
    } else if (query.status === 'found') {
      searchForm.type = 1
    }
  }
  if (query.keyword) {
    searchForm.keyword = query.keyword as string
  }
  if (query.category) {
    searchForm.category = query.category as string
  }
}

// 监听视图模式变化
watch(viewMode, (newMode) => {
  if (newMode === 'map' && items.value.length === 0) {
    fetchItems()
  }
})

onMounted(() => {
  initFromRoute()
  getUserLocation()
  fetchItems()
})
</script>

<style scoped lang="scss">
.items-page {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background: #f5f7fa;

  .container {
    max-width: 1400px;
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

.search-card {
  margin-bottom: 20px;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.search-section {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;

  .search-input {
    flex: 1;
    min-width: 200px;
    max-width: 400px;
  }
}

.filter-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;

  .filter-item {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .filter-label {
      width: 50px;
      color: #606266;
      font-size: 14px;
      flex-shrink: 0;
    }

    .filter-select {
      flex: 1;
    }

    .filter-date {
      flex: 1;
      width: 100%;
    }
  }

  .filter-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
  }
}

.toolbar-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;

  .toolbar-label {
    color: #606266;
    font-size: 14px;
    margin-right: 8px;
  }

  .sort-options,
  .view-toggle {
    display: flex;
    align-items: center;
  }

  .result-count {
    color: #909399;
    font-size: 14px;

    .count {
      color: #409eff;
      font-weight: 600;
    }
  }
}

.content-section {
  min-height: 400px;
}

.list-view {
  min-height: 300px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.map-view {
  height: 600px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

// 响应式设计
@media (max-width: 768px) {
  .items-page {
    padding: 12px;
  }

  .search-section {
    .search-input {
      width: 100%;
      max-width: none;
    }

    .el-button {
      flex: 1;
    }
  }

  .toolbar-section {
    flex-direction: column;
    align-items: flex-start;

    .sort-options,
    .view-toggle {
      width: 100%;
      justify-content: space-between;
    }

    .result-count {
      width: 100%;
      text-align: center;
    }
  }

  .map-view {
    height: 400px;
  }
}
</style>
