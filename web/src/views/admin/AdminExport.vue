<template>
  <div class="admin-export">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>数据导出</span>
        </div>
      </template>

      <!-- 导出表单 -->
      <el-form :model="exportForm" label-width="100px" class="export-form">
        <el-form-item label="时间范围" required>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            :disabled-date="disabledDate"
            style="width: 360px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            :disabled="!dateRange || dateRange.length !== 2"
            @click="handleExport"
          >
            <el-icon><Download /></el-icon>
            导出Excel报表
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 导出说明 -->
      <el-divider content-position="left">导出说明</el-divider>
      
      <el-alert type="info" :closable="false" class="export-info">
        <template #title>
          <span class="info-title">导出内容包括以下数据：</span>
        </template>
        <div class="info-content">
          <div class="info-section">
            <h4><el-icon><DataAnalysis /></el-icon> 概览数据</h4>
            <ul>
              <li>用户总数、新增用户数</li>
              <li>物品信息总数（失物/招领分类统计）</li>
              <li>匹配成功数量及匹配成功率</li>
              <li>今日新增用户和物品数</li>
            </ul>
          </div>
          
          <div class="info-section">
            <h4><el-icon><Calendar /></el-icon> 每日明细</h4>
            <ul>
              <li>按日期展示每天的新增用户数</li>
              <li>按日期展示每天的新增物品数</li>
              <li>按日期展示每天的匹配成功数</li>
            </ul>
          </div>
          
          <div class="info-section">
            <h4><el-icon><PieChart /></el-icon> 分类统计</h4>
            <ul>
              <li>按物品类别统计数量</li>
              <li>各类别占比分析</li>
            </ul>
          </div>
        </div>
      </el-alert>

      <!-- 导出提示 -->
      <el-alert 
        type="warning" 
        :closable="false" 
        style="margin-top: 16px"
        show-icon
      >
        <template #title>
          <span>温馨提示</span>
        </template>
        <ul class="tips-list">
          <li>导出的Excel文件包含多个工作表，分别对应不同类型的数据</li>
          <li>建议选择合理的时间范围，避免数据量过大导致导出时间过长</li>
          <li>导出的数据仅供内部分析使用，请妥善保管</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, DataAnalysis, Calendar, PieChart } from '@element-plus/icons-vue'
import { exportDataReport } from '@/api/admin'

// 表单数据
const exportForm = ref({})
const dateRange = ref<[string, string]>()
const loading = ref(false)

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
  {
    text: '本月',
    value: () => {
      const end = new Date()
      const start = new Date(end.getFullYear(), end.getMonth(), 1)
      return [start, end]
    },
  },
  {
    text: '上月',
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth() - 1, 1)
      const end = new Date(now.getFullYear(), now.getMonth(), 0)
      return [start, end]
    },
  },
]

// 禁用未来日期
const disabledDate = (time: Date) => {
  return time.getTime() > Date.now()
}

// 处理导出
const handleExport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  const [startDate, endDate] = dateRange.value

  // 验证日期范围
  const start = new Date(startDate)
  const end = new Date(endDate)
  const diffDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
  
  if (diffDays > 365) {
    ElMessage.warning('时间范围不能超过一年，请缩小查询范围')
    return
  }

  loading.value = true
  try {
    const blob = await exportDataReport({ startDate, endDate })

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `数据报表_${startDate}_${endDate}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    // 释放 URL 对象
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error: any) {
    console.error('Export error:', error)
    ElMessage.error(error.message || '导出失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.admin-export {
  .page-card {
    .card-header {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .export-form {
    margin-bottom: 20px;
  }

  .export-info {
    :deep(.el-alert__content) {
      width: 100%;
    }

    .info-title {
      font-weight: 600;
      font-size: 14px;
    }

    .info-content {
      display: flex;
      flex-wrap: wrap;
      gap: 24px;
      margin-top: 12px;

      .info-section {
        flex: 1;
        min-width: 200px;

        h4 {
          display: flex;
          align-items: center;
          gap: 6px;
          margin: 0 0 8px 0;
          font-size: 14px;
          color: #303133;
        }

        ul {
          margin: 0;
          padding-left: 20px;
          color: #606266;
          font-size: 13px;
          line-height: 1.8;

          li {
            margin-bottom: 4px;
          }
        }
      }
    }
  }

  .tips-list {
    margin: 8px 0 0 0;
    padding-left: 20px;
    font-size: 13px;
    line-height: 1.8;
    color: #606266;

    li {
      margin-bottom: 4px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .admin-export {
    .export-info {
      .info-content {
        flex-direction: column;
        gap: 16px;

        .info-section {
          min-width: 100%;
        }
      }
    }
  }
}
</style>
