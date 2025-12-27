<template>
  <div v-if="recommendations.length > 0" class="match-recommendations">
    <div class="section-header">
      <h2>可能匹配的物品</h2>
      <p>系统为您找到以下可能匹配的物品</p>
    </div>

    <el-row :gutter="20">
      <el-col
        v-for="match in recommendations"
        :key="match.id"
        :xs="24"
        :sm="12"
        :md="12"
        :lg="8"
      >
        <el-card class="match-card" shadow="hover">
          <div class="match-score">
            <span class="score-label">匹配度</span>
            <el-progress
              :percentage="getMatchPercentage(match.matchScore)"
              :color="getScoreColor(match.matchScore)"
              :stroke-width="8"
            />
          </div>

          <ItemCard :item="match" @click="handleItemClick(match)" />

          <div class="match-actions">
            <el-button
              type="primary"
              size="small"
              @click.stop="handleConfirmMatch(match)"
            >
              确认匹配
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ItemCard from '../item/ItemCard.vue'
import type { MatchVO } from '@/types'

const props = defineProps<{
  recommendations: MatchVO[]
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const router = useRouter()


const getMatchPercentage = (score: number): number => {
  return Math.min(Math.round(score), 100)
}

const getScoreColor = (score: number) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const handleItemClick = (item: MatchVO) => {
  router.push({ name: 'ItemDetail', params: { id: item.id } })
}

const handleConfirmMatch = async (match: MatchVO) => {
  try {
    await ElMessageBox.confirm(
      `确认与物品"${match.title}"匹配吗？确认后双方物品状态将更新为已找回。`,
      '确认匹配',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // Note: We need the source item ID to confirm match
    // This would need to be passed from the parent or stored in the match object
    ElMessage.warning('请从物品详情页确认匹配')
  } catch {
    // User cancelled
  }
}
</script>

<style scoped lang="scss">
.match-recommendations {
  margin-top: 40px;
}

.section-header {
  margin-bottom: 24px;
  text-align: center;

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  p {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.match-card {
  margin-bottom: 20px;
  border-radius: 8px;

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.match-score {
  margin-bottom: 16px;

  .score-label {
    display: block;
    font-size: 13px;
    color: #606266;
    margin-bottom: 8px;
    font-weight: 500;
  }
}

.match-actions {
  margin-top: 12px;
  text-align: center;

  .el-button {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .section-header {
    h2 {
      font-size: 20px;
    }
  }
}
</style>
