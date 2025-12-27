<template>
  <div class="home">
    <div class="container">
      <div class="page-header">
        <h1>Campus Lost & Found</h1>
        <p>Find what you've lost, return what you've found</p>
      </div>

      <div class="page-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <h2>Lost Something?</h2>
              <p>Browse found items or post about what you've lost</p>
              <el-button type="primary" @click="goToItems('found')">
                Browse Found Items
              </el-button>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <h2>Found Something?</h2>
              <p>Browse lost items or post about what you've found</p>
              <el-button type="success" @click="goToItems('lost')">
                Browse Lost Items
              </el-button>
            </el-card>
          </el-col>
        </el-row>

        <MatchRecommendationList
          v-if="isAuthenticated"
          :recommendations="recommendations"
          @refresh="fetchRecommendations"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserMatchRecommendations } from '@/api/match'
import MatchRecommendationList from '@/components/match/MatchRecommendationList.vue'
import type { MatchVO } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const recommendations = ref<MatchVO[]>([])

const isAuthenticated = computed(() => userStore.isLoggedIn)

const goToItems = (status: string) => {
  router.push({ name: 'Items', query: { status } })
}

const fetchRecommendations = async () => {
  if (!isAuthenticated.value) {
    return
  }

  try {
    const response = await getUserMatchRecommendations()
    if (response.data) {
      recommendations.value = response.data
    }
  } catch (error) {
    console.error('Failed to fetch recommendations:', error)
  }
}

onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped lang="scss">
.home {
  h2 {
    margin-bottom: 12px;
  }

  p {
    margin-bottom: 16px;
    color: #909399;
  }
}
</style>
