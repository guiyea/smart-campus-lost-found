<template>
  <div class="item-map">
    <!-- 地图容器 -->
    <div ref="mapContainer" class="map-container"></div>

    <!-- 图例 -->
    <div class="map-legend">
      <div class="legend-item">
        <span class="legend-marker lost"></span>
        <span class="legend-text">失物</span>
      </div>
      <div class="legend-item">
        <span class="legend-marker found"></span>
        <span class="legend-text">招领</span>
      </div>
    </div>

    <!-- 信息弹窗 -->
    <div
      v-if="selectedItem"
      ref="infoWindowRef"
      class="info-window"
    >
      <div class="info-window-content">
        <div class="info-image">
          <el-image
            :src="selectedItem.images?.[0] || defaultImage"
            fit="cover"
            class="image"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <el-tag
            :type="selectedItem.type === 0 ? 'danger' : 'success'"
            class="type-tag"
            size="small"
            effect="dark"
          >
            {{ selectedItem.type === 0 ? '失物' : '招领' }}
          </el-tag>
        </div>
        <div class="info-details">
          <h4 class="info-title">{{ selectedItem.title }}</h4>
          <p class="info-location">
            <el-icon><Location /></el-icon>
            {{ selectedItem.locationDesc }}
          </p>
          <p class="info-time">
            <el-icon><Clock /></el-icon>
            {{ formatDate(selectedItem.eventTime) }}
          </p>
          <el-button
            type="primary"
            size="small"
            @click="handleViewDetail"
          >
            查看详情
          </el-button>
        </div>
        <el-icon class="close-btn" @click="closeInfoWindow"><Close /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Location, Clock, Picture, Close } from '@element-plus/icons-vue'
import type { ItemVO } from '@/types'

// Props
const props = withDefaults(defineProps<{
  items: ItemVO[]
  center?: { lng: number; lat: number }
  zoom?: number
}>(), {
  center: () => ({ lng: 116.397428, lat: 39.90923 }),
  zoom: 13
})

// Emits
const emit = defineEmits<{
  (e: 'marker-click', item: ItemVO): void
}>()

// 默认图片
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjgwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9IiNmNWY3ZmEiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjEyIiBmaWxsPSIjYzBjNGNjIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5pqC5peg5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='

// Refs
const mapContainer = ref<HTMLDivElement | null>(null)
const infoWindowRef = ref<HTMLDivElement | null>(null)

// 状态
const selectedItem = ref<ItemVO | null>(null)

// 高德地图实例
let map: any = null
let markers: any[] = []
let infoWindow: any = null

// 失物标记图标（红色）
const lostIcon = {
  url: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTE2IDAgQzI0LjgzNyAwIDMyIDcuMTYzIDMyIDE2IEMzMiAyOCAxNiA0MCAxNiA0MCBDMTYgNDAgMCAyOCAwIDE2IEMwIDcuMTYzIDcuMTYzIDAgMTYgMCBaIiBmaWxsPSIjRjU2QzZDIi8+PGNpcmNsZSBjeD0iMTYiIGN5PSIxNCIgcj0iNiIgZmlsbD0id2hpdGUiLz48L3N2Zz4=',
  size: [32, 40] as [number, number],
  anchor: [16, 40] as [number, number]
}

// 招领标记图标（绿色）
const foundIcon = {
  url: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTE2IDAgQzI0LjgzNyAwIDMyIDcuMTYzIDMyIDE2IEMzMiAyOCAxNiA0MCAxNiA0MCBDMTYgNDAgMCAyOCAwIDE2IEMwIDcuMTYzIDcuMTYzIDAgMTYgMCBaIiBmaWxsPSIjNjdDMjNBIi8+PGNpcmNsZSBjeD0iMTYiIGN5PSIxNCIgcj0iNiIgZmlsbD0id2hpdGUiLz48L3N2Zz4=',
  size: [32, 40] as [number, number],
  anchor: [16, 40] as [number, number]
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 初始化地图
const initMap = () => {
  if (!mapContainer.value) return

  // 检查高德地图API是否加载
  if (typeof AMap === 'undefined') {
    console.error('高德地图API未加载')
    return
  }

  // 创建地图实例
  map = new AMap.Map(mapContainer.value, {
    zoom: props.zoom,
    center: [props.center.lng, props.center.lat],
    resizeEnable: true,
    mapStyle: 'amap://styles/normal'
  })

  // 添加控件
  AMap.plugin(['AMap.Scale', 'AMap.ToolBar'], () => {
    map.addControl(new AMap.Scale())
    map.addControl(new AMap.ToolBar({
      position: 'RB'
    }))
  })

  // 点击地图关闭信息窗口
  map.on('click', () => {
    closeInfoWindow()
  })

  // 添加标记
  addMarkers()
}

// 添加标记
const addMarkers = () => {
  if (!map) return

  // 清除现有标记
  clearMarkers()

  // 添加新标记
  props.items.forEach((item) => {
    if (item.longitude && item.latitude) {
      const iconConfig = item.type === 0 ? lostIcon : foundIcon

      const marker = new AMap.Marker({
        position: [item.longitude, item.latitude],
        icon: new AMap.Icon({
          size: new AMap.Size(iconConfig.size[0], iconConfig.size[1]),
          image: iconConfig.url,
          imageSize: new AMap.Size(iconConfig.size[0], iconConfig.size[1])
        }),
        offset: new AMap.Pixel(-iconConfig.anchor[0], -iconConfig.anchor[1]),
        cursor: 'pointer',
        extData: item
      })

      // 点击标记显示信息窗口
      marker.on('click', () => {
        showInfoWindow(item, marker)
      })

      marker.setMap(map)
      markers.push(marker)
    }
  })

  // 自适应视野
  if (markers.length > 0) {
    map.setFitView(markers, false, [50, 50, 50, 50])
  }
}

// 清除标记
const clearMarkers = () => {
  markers.forEach((marker) => {
    marker.setMap(null)
  })
  markers = []
}

// 显示信息窗口
const showInfoWindow = (item: ItemVO, marker: any) => {
  selectedItem.value = item

  // 等待DOM更新后创建信息窗口
  nextTick(() => {
    if (infoWindow) {
      infoWindow.close()
    }

    if (infoWindowRef.value) {
      infoWindow = new AMap.InfoWindow({
        isCustom: true,
        content: infoWindowRef.value,
        offset: new AMap.Pixel(0, -45)
      })

      infoWindow.open(map, marker.getPosition())
    }
  })
}

// 关闭信息窗口
const closeInfoWindow = () => {
  selectedItem.value = null
  if (infoWindow) {
    infoWindow.close()
  }
}

// 查看详情
const handleViewDetail = () => {
  if (selectedItem.value) {
    emit('marker-click', selectedItem.value)
  }
}

// 监听物品列表变化
watch(
  () => props.items,
  () => {
    if (map) {
      addMarkers()
    }
  },
  { deep: true }
)

// 监听中心点变化
watch(
  () => props.center,
  (newCenter) => {
    if (map && newCenter) {
      map.setCenter([newCenter.lng, newCenter.lat])
    }
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  setTimeout(() => {
    initMap()
  }, 100)
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped lang="scss">
.item-map {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 16px;
  z-index: 100;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .legend-marker {
    width: 12px;
    height: 12px;
    border-radius: 50%;

    &.lost {
      background: #f56c6c;
    }

    &.found {
      background: #67c23a;
    }
  }

  .legend-text {
    font-size: 13px;
    color: #606266;
  }
}

.info-window {
  position: absolute;
  z-index: 200;

  .info-window-content {
    position: relative;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    width: 280px;

    &::after {
      content: '';
      position: absolute;
      bottom: -8px;
      left: 50%;
      transform: translateX(-50%);
      border-left: 8px solid transparent;
      border-right: 8px solid transparent;
      border-top: 8px solid white;
    }
  }

  .info-image {
    position: relative;
    height: 120px;

    .image {
      width: 100%;
      height: 100%;
    }

    .image-placeholder {
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

    .type-tag {
      position: absolute;
      top: 8px;
      left: 8px;
    }
  }

  .info-details {
    padding: 12px;

    .info-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .info-location,
    .info-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #909399;
      margin-bottom: 6px;

      .el-icon {
        flex-shrink: 0;
      }
    }

    .el-button {
      width: 100%;
      margin-top: 8px;
    }
  }

  .close-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    transition: background 0.3s;

    &:hover {
      background: rgba(0, 0, 0, 0.7);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .map-legend {
    bottom: 10px;
    left: 10px;
    padding: 8px 12px;
    gap: 12px;

    .legend-text {
      font-size: 12px;
    }
  }

  .info-window {
    .info-window-content {
      width: 240px;
    }

    .info-image {
      height: 100px;
    }
  }
}
</style>
