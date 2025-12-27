<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon users">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalUsers }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="today-data">今日新增: {{ statistics.todayNewUsers }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon items">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalItems }}</div>
              <div class="stat-label">信息总数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="today-data">今日新增: {{ statistics.todayNewItems }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon matched">
              <el-icon :size="32"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalMatched }}</div>
              <div class="stat-label">匹配成功数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="match-rate">匹配率: {{ (statistics.matchRate * 100).toFixed(1) }}%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon rate">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (statistics.matchRate * 100).toFixed(1) }}%</div>
              <div class="stat-label">匹配成功率</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="lost-found">失物: {{ statistics.totalLostItems }} / 招领: {{ statistics.totalFoundItems }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 近7天数据趋势折线图 -->
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近7天数据趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="trendChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <!-- 物品类别分布饼图 -->
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>失物/招领比例</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="typeChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 物品类别分布柱状图 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>物品类别分布</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="categoryChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <!-- 每日新增对比柱状图 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>每日新增对比</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="dailyCompareChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { User, Document, Connection, TrendCharts } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import type { StatisticsVO, DailyStatVO } from '../../types'
import { getStatistics } from '../../api/admin'
import { ElMessage } from 'element-plus'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 统计数据
const statistics = ref<StatisticsVO>({
  totalUsers: 0,
  totalItems: 0,
  totalLostItems: 0,
  totalFoundItems: 0,
  totalMatched: 0,
  matchRate: 0,
  todayNewUsers: 0,
  todayNewItems: 0,
  weeklyTrend: []
})

const loading = ref(false)

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getStatistics()
    if (res.code === 200 && res.data) {
      statistics.value = res.data
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 近7天数据趋势折线图配置
const trendChartOption = computed(() => {
  const trend = statistics.value.weeklyTrend || []
  const dates = trend.map((item: DailyStatVO) => item.date)
  const newUsers = trend.map((item: DailyStatVO) => item.newUsers)
  const newItems = trend.map((item: DailyStatVO) => item.newItems)
  const matchedCount = trend.map((item: DailyStatVO) => item.matchedCount)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['新增用户', '新增信息', '匹配成功'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        formatter: (value: string) => {
          const date = new Date(value)
          return `${date.getMonth() + 1}/${date.getDate()}`
        }
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: newUsers,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ]
          }
        }
      },
      {
        name: '新增信息',
        type: 'line',
        smooth: true,
        data: newItems,
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
            ]
          }
        }
      },
      {
        name: '匹配成功',
        type: 'line',
        smooth: true,
        data: matchedCount,
        itemStyle: { color: '#E6A23C' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
              { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
            ]
          }
        }
      }
    ]
  }
})

// 失物/招领比例饼图配置
const typeChartOption = computed(() => {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      data: ['失物', '招领']
    },
    series: [
      {
        name: '物品类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { 
            value: statistics.value.totalLostItems, 
            name: '失物',
            itemStyle: { color: '#F56C6C' }
          },
          { 
            value: statistics.value.totalFoundItems, 
            name: '招领',
            itemStyle: { color: '#67C23A' }
          }
        ]
      }
    ]
  }
})

// 物品类别分布柱状图配置
const categoryChartOption = computed(() => {
  // 预定义的物品类别
  const categories = ['电子设备', '证件卡片', '钥匙', '钱包', '书籍文具', '衣物配饰', '运动器材', '其他']
  
  // 模拟数据（实际应从后端获取）
  const lostData = [15, 8, 12, 6, 10, 5, 3, 7]
  const foundData = [12, 10, 8, 5, 8, 4, 2, 5]

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['失物', '招领'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '失物',
        type: 'bar',
        data: lostData,
        itemStyle: { color: '#F56C6C' }
      },
      {
        name: '招领',
        type: 'bar',
        data: foundData,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
})

// 每日新增对比柱状图配置
const dailyCompareChartOption = computed(() => {
  const trend = statistics.value.weeklyTrend || []
  const dates = trend.map((item: DailyStatVO) => {
    const date = new Date(item.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const newUsers = trend.map((item: DailyStatVO) => item.newUsers)
  const newItems = trend.map((item: DailyStatVO) => item.newItems)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['新增用户', '新增信息'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: newUsers,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '新增信息',
        type: 'bar',
        data: newItems,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
})

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
  
  .stat-content {
    display: flex;
    align-items: center;
    padding: 10px 0;
  }
  
  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    
    &.users {
      background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
      color: #fff;
    }
    
    &.items {
      background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
      color: #fff;
    }
    
    &.matched {
      background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
      color: #fff;
    }
    
    &.rate {
      background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
      color: #fff;
    }
  }
  
  .stat-info {
    flex: 1;
  }
  
  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
  
  .stat-footer {
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
    font-size: 13px;
    color: #606266;
    
    .today-data {
      color: #409EFF;
    }
    
    .match-rate {
      color: #67C23A;
    }
    
    .lost-found {
      color: #909399;
    }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .chart-container {
    height: 300px;
    width: 100%;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }
  
  .stat-card {
    .stat-icon {
      width: 48px;
      height: 48px;
    }
    
    .stat-value {
      font-size: 22px;
    }
  }
  
  .chart-card {
    .chart-container {
      height: 250px;
    }
  }
}
</style>
