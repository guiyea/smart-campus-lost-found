<template>
  <div class="location-picker">
    <!-- 搜索框 -->
    <div class="search-box">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索地址..."
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch" :loading="searching">
            搜索
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 搜索结果列表 -->
    <div v-if="searchResults.length > 0" class="search-results">
      <div
        v-for="(result, index) in searchResults"
        :key="index"
        class="search-result-item"
        @click="selectSearchResult(result)"
      >
        <el-icon><Location /></el-icon>
        <span>{{ result.name }}</span>
        <span class="address">{{ result.address }}</span>
      </div>
    </div>

    <!-- 地图容器 -->
    <div ref="mapContainer" class="map-container"></div>

    <!-- 当前位置信息 -->
    <div class="location-info">
      <div class="info-row">
        <span class="label">当前位置：</span>
        <span class="value">{{ currentAddress || '请在地图上选择位置' }}</span>
      </div>
      <div class="info-row coordinates" v-if="modelValue?.longitude && modelValue?.latitude">
        <span class="label">坐标：</span>
        <span class="value">{{ modelValue.longitude.toFixed(6) }}, {{ modelValue.latitude.toFixed(6) }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="getCurrentLocation" :loading="locating">
        <el-icon><Aim /></el-icon>
        获取当前位置
      </el-button>
      <el-button @click="clearLocation">
        <el-icon><Delete /></el-icon>
        清除
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { Search, Location, Aim, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 定义位置数据类型
interface LocationData {
  longitude: number
  latitude: number
  address: string
}

// 搜索结果类型
interface SearchResult {
  name: string
  address: string
  location: {
    lng: number
    lat: number
  }
}

// Props
const props = defineProps<{
  modelValue?: LocationData | null
  defaultCenter?: { lng: number; lat: number }
  defaultZoom?: number
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: LocationData | null): void
}>()

// 响应式数据
const mapContainer = ref<HTMLDivElement | null>(null)
const searchKeyword = ref('')
const searchResults = ref<SearchResult[]>([])
const currentAddress = ref('')
const searching = ref(false)
const locating = ref(false)

// 高德地图实例
let map: any = null
let marker: any = null
let geocoder: any = null
let placeSearch: any = null

// 默认中心点（北京）
const defaultCenter = props.defaultCenter || { lng: 116.397428, lat: 39.90923 }
const defaultZoom = props.defaultZoom || 15

// 初始化地图
const initMap = () => {
  if (!mapContainer.value) return

  // 检查高德地图API是否加载
  if (typeof AMap === 'undefined') {
    console.error('高德地图API未加载')
    ElMessage.error('地图加载失败，请刷新页面重试')
    return
  }

  // 创建地图实例
  map = new AMap.Map(mapContainer.value, {
    zoom: defaultZoom,
    center: [defaultCenter.lng, defaultCenter.lat],
    resizeEnable: true,
  })

  // 创建标记
  marker = new AMap.Marker({
    position: [defaultCenter.lng, defaultCenter.lat],
    draggable: true,
    cursor: 'move',
  })

  // 如果有初始值，设置标记位置
  if (props.modelValue?.longitude && props.modelValue?.latitude) {
    const pos = [props.modelValue.longitude, props.modelValue.latitude]
    marker.setPosition(pos)
    map.setCenter(pos)
    currentAddress.value = props.modelValue.address || ''
  }

  marker.setMap(map)

  // 加载插件
  AMap.plugin(['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.Geolocation'], () => {
    // 地理编码服务
    geocoder = new AMap.Geocoder({
      city: '全国',
    })

    // 地点搜索服务
    placeSearch = new AMap.PlaceSearch({
      city: '全国',
      pageSize: 10,
    })
  })

  // 地图点击事件
  map.on('click', (e: any) => {
    const lnglat = e.lnglat
    updateLocation(lnglat.getLng(), lnglat.getLat())
  })

  // 标记拖拽结束事件
  marker.on('dragend', () => {
    const position = marker.getPosition()
    updateLocation(position.getLng(), position.getLat())
  })
}

// 更新位置
const updateLocation = (lng: number, lat: number) => {
  // 更新标记位置
  if (marker) {
    marker.setPosition([lng, lat])
  }

  // 逆地理编码获取地址
  if (geocoder) {
    geocoder.getAddress([lng, lat], (status: string, result: any) => {
      if (status === 'complete' && result.regeocode) {
        const address = result.regeocode.formattedAddress
        currentAddress.value = address
        
        // 触发更新
        emit('update:modelValue', {
          longitude: lng,
          latitude: lat,
          address: address,
        })
      } else {
        currentAddress.value = `${lng.toFixed(6)}, ${lat.toFixed(6)}`
        emit('update:modelValue', {
          longitude: lng,
          latitude: lat,
          address: currentAddress.value,
        })
      }
    })
  } else {
    currentAddress.value = `${lng.toFixed(6)}, ${lat.toFixed(6)}`
    emit('update:modelValue', {
      longitude: lng,
      latitude: lat,
      address: currentAddress.value,
    })
  }
}

// 搜索地址
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  if (!placeSearch) {
    ElMessage.error('搜索服务未初始化')
    return
  }

  searching.value = true
  placeSearch.search(searchKeyword.value, (status: string, result: any) => {
    searching.value = false
    if (status === 'complete' && result.poiList) {
      searchResults.value = result.poiList.pois.map((poi: any) => ({
        name: poi.name,
        address: poi.address || poi.cityname + poi.adname,
        location: {
          lng: poi.location.getLng(),
          lat: poi.location.getLat(),
        },
      }))
    } else {
      searchResults.value = []
      ElMessage.info('未找到相关地点')
    }
  })
}

// 选择搜索结果
const selectSearchResult = (result: SearchResult) => {
  const { lng, lat } = result.location
  
  // 移动地图中心
  if (map) {
    map.setCenter([lng, lat])
    map.setZoom(16)
  }

  // 更新位置
  updateLocation(lng, lat)
  
  // 清空搜索结果
  searchResults.value = []
  searchKeyword.value = ''
}

// 获取当前位置
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持定位功能')
    return
  }

  locating.value = true
  
  // 使用高德地图定位
  AMap.plugin('AMap.Geolocation', () => {
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      buttonPosition: 'RB',
      zoomToAccuracy: true,
    })

    geolocation.getCurrentPosition((status: string, result: any) => {
      locating.value = false
      if (status === 'complete') {
        const { lng, lat } = result.position
        if (map) {
          map.setCenter([lng, lat])
          map.setZoom(16)
        }
        updateLocation(lng, lat)
        ElMessage.success('定位成功')
      } else {
        // 降级使用浏览器定位
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lng = position.coords.longitude
            const lat = position.coords.latitude
            if (map) {
              map.setCenter([lng, lat])
              map.setZoom(16)
            }
            updateLocation(lng, lat)
            ElMessage.success('定位成功')
          },
          (error) => {
            console.error('定位失败:', error)
            ElMessage.error('定位失败，请手动选择位置')
          },
          {
            enableHighAccuracy: true,
            timeout: 10000,
          }
        )
      }
    })
  })
}

// 清除位置
const clearLocation = () => {
  currentAddress.value = ''
  emit('update:modelValue', null)
  
  // 重置标记到默认位置
  if (marker) {
    marker.setPosition([defaultCenter.lng, defaultCenter.lat])
  }
  if (map) {
    map.setCenter([defaultCenter.lng, defaultCenter.lat])
    map.setZoom(defaultZoom)
  }
}

// 监听外部值变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal?.longitude && newVal?.latitude) {
      const pos = [newVal.longitude, newVal.latitude]
      if (marker) {
        marker.setPosition(pos)
      }
      if (map) {
        map.setCenter(pos)
      }
      currentAddress.value = newVal.address || ''
    }
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  // 等待DOM渲染完成后初始化地图
  setTimeout(() => {
    initMap()
  }, 100)
})

onUnmounted(() => {
  // 销毁地图实例
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped lang="scss">
.location-picker {
  width: 100%;
  
  .search-box {
    margin-bottom: 12px;
  }

  .search-results {
    max-height: 200px;
    overflow-y: auto;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    margin-bottom: 12px;
    background: #fff;

    .search-result-item {
      padding: 10px 12px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 8px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background-color: #f5f7fa;
      }

      .el-icon {
        color: #409eff;
        flex-shrink: 0;
      }

      span {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .address {
        color: #909399;
        font-size: 12px;
        margin-left: auto;
      }
    }
  }

  .map-container {
    width: 100%;
    height: 350px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    margin-bottom: 12px;
  }

  .location-info {
    background: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
    margin-bottom: 12px;

    .info-row {
      display: flex;
      align-items: flex-start;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: #606266;
        flex-shrink: 0;
        width: 80px;
      }

      .value {
        color: #303133;
        word-break: break-all;
      }
    }

    .coordinates {
      .value {
        font-family: monospace;
        font-size: 13px;
        color: #909399;
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
  }
}
</style>
